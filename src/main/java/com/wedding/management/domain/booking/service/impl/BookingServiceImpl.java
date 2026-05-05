package com.wedding.management.domain.booking.service.impl;

import com.wedding.management.common.audit.AuditLog;
import com.wedding.management.common.audit.AuditLogRepository;
import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.domain.booking.dto.*;
import com.wedding.management.domain.booking.enums.*;
import com.wedding.management.domain.booking.model.*;
import com.wedding.management.domain.booking.repository.*;
import com.wedding.management.domain.booking.service.BookingDocumentService;
import com.wedding.management.domain.booking.service.BookingPaymentReader;
import com.wedding.management.domain.booking.service.BookingService;
import com.wedding.management.domain.hall.enums.DayType;
import com.wedding.management.domain.hall.enums.HallStatus;
import com.wedding.management.domain.hall.enums.TimeSlot;
import com.wedding.management.domain.hall.model.Hall;
import com.wedding.management.domain.hall.model.HallPricing;
import com.wedding.management.domain.hall.repository.HallPricingRepository;
import com.wedding.management.domain.hall.repository.HallRepository;
import com.wedding.management.domain.menu.enums.BeverageStatus;
import com.wedding.management.domain.menu.enums.DishComboStatus;
import com.wedding.management.domain.menu.enums.DishStatus;
import com.wedding.management.domain.menu.model.*;
import com.wedding.management.domain.menu.repository.*;
import com.wedding.management.domain.service.enums.ServiceStatus;
import com.wedding.management.domain.service.repository.ServiceRepository;
import com.wedding.management.domain.shift.enums.ShiftStatus;
import com.wedding.management.domain.shift.model.Shift;
import com.wedding.management.domain.shift.repository.ShiftRepository;
import com.wedding.management.domain.weddingpackage.enums.WeddingPackageStatus;
import com.wedding.management.domain.weddingpackage.model.*;
import com.wedding.management.domain.weddingpackage.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private static final int HOLD_DURATION_MINUTES = 15;
    private static final double DEFAULT_DEPOSIT_RATE = 0.30;
    private static final double DEFAULT_TAX_RATE = 0.10;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(0|\\+84)[0-9]{9,10}$");

    private final BookingRepository bookingRepository;
    private final BookingLineSnapshotRepository lineRepository;
    private final BookingPackageSnapshotRepository packageSnapshotRepository;
    private final BookingHallHoldRepository holdRepository;
    private final HallRepository hallRepository;
    private final HallPricingRepository hallPricingRepository;
    private final ShiftRepository shiftRepository;
    private final WeddingPackageRepository weddingPackageRepository;
    private final WeddingPackageMenuComboRepository packageMenuComboRepository;
    private final WeddingPackageServiceItemRepository packageServiceItemRepository;
    private final WeddingPackageBeverageAllowanceRepository packageBeverageAllowanceRepository;
    private final WeddingPackageBenefitRepository packageBenefitRepository;
    private final DishRepository dishRepository;
    private final DishComboRepository dishComboRepository;
    private final DishComboSlotRepository dishComboSlotRepository;
    private final BeverageRepository beverageRepository;
    private final ServiceRepository serviceRepository;
    private final AuditLogRepository auditLogRepository;
    private final BookingDocumentService bookingDocumentService;
    private final BookingPaymentReader bookingPaymentReader;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            BookingLineSnapshotRepository lineRepository,
            BookingPackageSnapshotRepository packageSnapshotRepository,
            BookingHallHoldRepository holdRepository,
            HallRepository hallRepository,
            HallPricingRepository hallPricingRepository,
            ShiftRepository shiftRepository,
            WeddingPackageRepository weddingPackageRepository,
            WeddingPackageMenuComboRepository packageMenuComboRepository,
            WeddingPackageServiceItemRepository packageServiceItemRepository,
            WeddingPackageBeverageAllowanceRepository packageBeverageAllowanceRepository,
            WeddingPackageBenefitRepository packageBenefitRepository,
            DishRepository dishRepository,
            DishComboRepository dishComboRepository,
            DishComboSlotRepository dishComboSlotRepository,
            BeverageRepository beverageRepository,
            ServiceRepository serviceRepository,
            AuditLogRepository auditLogRepository,
            BookingDocumentService bookingDocumentService,
            BookingPaymentReader bookingPaymentReader
    ) {
        this.bookingRepository = bookingRepository;
        this.lineRepository = lineRepository;
        this.packageSnapshotRepository = packageSnapshotRepository;
        this.holdRepository = holdRepository;
        this.hallRepository = hallRepository;
        this.hallPricingRepository = hallPricingRepository;
        this.shiftRepository = shiftRepository;
        this.weddingPackageRepository = weddingPackageRepository;
        this.packageMenuComboRepository = packageMenuComboRepository;
        this.packageServiceItemRepository = packageServiceItemRepository;
        this.packageBeverageAllowanceRepository = packageBeverageAllowanceRepository;
        this.packageBenefitRepository = packageBenefitRepository;
        this.dishRepository = dishRepository;
        this.dishComboRepository = dishComboRepository;
        this.dishComboSlotRepository = dishComboSlotRepository;
        this.beverageRepository = beverageRepository;
        this.serviceRepository = serviceRepository;
        this.auditLogRepository = auditLogRepository;
        this.bookingDocumentService = bookingDocumentService;
        this.bookingPaymentReader = bookingPaymentReader;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HallAvailabilityResponse> checkHallAvailability(LocalDate bookingDate, UUID shiftId, Integer capacity) {
        // BR-CHA-1 / BR-CHA-2 / BR-CHA-3 / BR-CHA-4
        if (bookingDate == null || shiftId == null || capacity == null || capacity <= 0) {
            throw new BadRequestException("MSG2: Ngày, ca và số bàn không được để trống");
        }

        Shift shift = shiftRepository.findByIdAndIsDeletedFalse(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Ca không tồn tại"));

        Instant bookingInstant = toStartOfDay(bookingDate);
        List<BookingStatus> blockingStatuses = List.of(BookingStatus.CONFIRMED, BookingStatus.ONGOING, BookingStatus.PENDING);
        List<Booking> booked = bookingRepository.findByBookingDateAndShiftAndStatuses(bookingInstant, shiftId, blockingStatuses);
        Set<UUID> bookedHallIds = booked.stream().map(b -> b.getHall().getId()).collect(Collectors.toSet());

        return hallRepository.findAllActive().stream()
                .filter(h -> h.getStatus() == HallStatus.ACTIVE)
                .filter(h -> h.getMaxTables() != null && h.getMaxTables() >= capacity)
                .filter(h -> !bookedHallIds.contains(h.getId()))
                .map(h -> HallAvailabilityResponse.builder()
                        .hallId(h.getId())
                        .hallName(h.getName())
                        .hallTypeId(h.getHallType().getId())
                        .hallTypeName(h.getHallType().getName())
                        .hallImage(h.getHallImage())
                        .price(calculateHallPrice(h, shift, bookingDate))
                        .maxTables(h.getMaxTables())
                        .description(h.getDescription())
                        .status(h.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse createBooking(BookingRequest request, String currentUserId) {
        // BR-CBK-10: ValidateWeddingInput + ValidateTableInput
        validateBookingInput(request, true);

        Hall hall = loadActiveHall(request.getHallId());
        Shift shift = loadActiveShift(request.getShiftId());
        Instant bookingInstant = toStartOfDay(request.getBookingDate());

        validateTableInput(request.getNumberOfTables(), request.getNumberOfReserveTables(), hall);

        // BR-CBK-11: Hall availability recheck + hold validation
        ensureHallSlotAvailable(hall.getId(), bookingInstant, shift.getId(), null);
        validateOrCreateHold(hall, shift, bookingInstant, currentUserId);

        WeddingPackage weddingPackage = null;
        DishCombo selectedMenuCombo = null;
        if (request.getBookingMode() == BookingMode.PACKAGE) {
            weddingPackage = loadActiveWeddingPackage(request.getPackageId());
            selectedMenuCombo = resolveSelectedMenuCombo(weddingPackage, request.getSelectedMenuComboId());
        }

        double hallPrice = calculateHallPrice(hall, shift, request.getBookingDate());
        List<BookingLineRequest> draftLines = initializeDraftLines(request, weddingPackage, selectedMenuCombo, hall, hallPrice);
        AmountSummary amountSummary = recalculateAmount(draftLines);
        double deposit = request.getDepositAmount() == null ? round(amountSummary.bookingAmount * DEFAULT_DEPOSIT_RATE) : request.getDepositAmount();
        if (deposit <= 0) throw new BadRequestException("MSG2: Tiền đặt cọc phải lớn hơn 0");

        // BR-CBK-12: CreateBooking(...)
        Booking booking = Booking.builder()
                .bookingDate(bookingInstant)
                .shift(shift)
                .hall(hall)
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .brideName(request.getBrideName())
                .groomName(request.getGroomName())
                .weddingDate(toStartOfDay(request.getWeddingDate()))
                .numberOfTables(request.getNumberOfTables())
                .numberOfReserveTables(request.getNumberOfReserveTables())
                .bookingMode(request.getBookingMode())
                .weddingPackage(weddingPackage)
                .selectedMenuCombo(selectedMenuCombo)
                .hallPrice(hallPrice)
                .subtotalAmount(amountSummary.subtotalAmount)
                .taxAmount(amountSummary.taxAmount)
                .bookingAmount(amountSummary.bookingAmount)
                .depositAmount(deposit)
                .confirmedPaymentAmount(0.0)
                .remainingAmount(round(amountSummary.bookingAmount - 0.0))
                .note(request.getNote())
                .status(BookingStatus.PENDING)
                .createdBy(currentUserId)
                .createdAt(Instant.now())
                .isDeleted(false)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        saveLineSnapshots(savedBooking, draftLines, currentUserId);
        savePackageSnapshot(savedBooking, weddingPackage, selectedMenuCombo, currentUserId);
        convertHold(hall.getId(), shift.getId(), bookingInstant, currentUserId, savedBooking);

        // BR-CBK-13 / BR-CBK-14
        bookingDocumentService.generateConfirmationDocument(savedBooking.getId());
        saveAuditLog(currentUserId, "CREATE_BOOKING", savedBooking.getId(), savedBooking.getCustomerName());

        return mapToBookingResponse(savedBooking);
    }

    @Override
    public BookingResponse updateBooking(UUID bookingId, BookingRequest request, String currentUserId, long lastModifiedAt) {
        Booking booking = loadExistingBooking(bookingId);

        // BR-UBK-1: only PENDING can be updated
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("MSG67: Trạng thái booking không cho phép cập nhật");
        }

        // BR-UBK-8: optimistic locking
        if (booking.getUpdatedAt() != null && booking.getUpdatedAt().toEpochMilli() != lastModifiedAt) {
            throw new BadRequestException("MSG62: Dữ liệu đã được sửa đổi bởi người khác. Vui lòng tải lại trang.");
        }

        validateBookingInput(request, false);

        Hall newHall = loadActiveHall(request.getHallId());
        Shift newShift = loadActiveShift(request.getShiftId());
        Instant newBookingDate = toStartOfDay(request.getBookingDate());
        validateTableInput(request.getNumberOfTables(), request.getNumberOfReserveTables(), newHall);

        boolean slotChanged = !booking.getHall().getId().equals(newHall.getId())
                || !booking.getShift().getId().equals(newShift.getId())
                || !booking.getBookingDate().equals(newBookingDate);

        // BR-UBK-2 / BR-UBK-9: slot change and availability recheck
        if (slotChanged) {
            ensureHallSlotAvailable(newHall.getId(), newBookingDate, newShift.getId(), bookingId);
            validateOrCreateHold(newHall, newShift, newBookingDate, currentUserId);
        }

        WeddingPackage newPackage = null;
        DishCombo newSelectedCombo = null;
        if (request.getBookingMode() == BookingMode.PACKAGE) {
            newPackage = loadActiveWeddingPackage(request.getPackageId());
            newSelectedCombo = resolveSelectedMenuCombo(newPackage, request.getSelectedMenuComboId());
        }

        boolean packageChanged = detectPackageChange(booking, request.getBookingMode(), newPackage, newSelectedCombo);
        List<BookingLineRequest> draftLines;
        if (packageChanged) {
            draftLines = initializeDraftLines(request, newPackage, newSelectedCombo, newHall, calculateHallPrice(newHall, newShift, request.getBookingDate()));
        } else {
            draftLines = request.getBookingDraftLines() == null || request.getBookingDraftLines().isEmpty()
                    ? lineRepository.findByBookingId(bookingId).stream().map(this::toLineRequest).collect(Collectors.toList())
                    : initializeDraftLines(request, newPackage, newSelectedCombo, newHall, calculateHallPrice(newHall, newShift, request.getBookingDate()));
        }

        AmountSummary amountSummary = recalculateAmount(draftLines);
        double confirmedPaid = bookingPaymentReader.getConfirmedPaymentAmountByBooking(bookingId);
        double deposit = confirmedPaid > 0 ? booking.getDepositAmount()
                : (request.getDepositAmount() == null ? round(amountSummary.bookingAmount * DEFAULT_DEPOSIT_RATE) : request.getDepositAmount());

        // BR-UBK-10: UpdateBooking(...)
        booking.setBookingDate(newBookingDate);
        booking.setShift(newShift);
        booking.setHall(newHall);
        booking.setCustomerName(request.getCustomerName());
        booking.setCustomerPhone(request.getCustomerPhone());
        booking.setCustomerEmail(request.getCustomerEmail());
        booking.setBrideName(request.getBrideName());
        booking.setGroomName(request.getGroomName());
        booking.setWeddingDate(toStartOfDay(request.getWeddingDate()));
        booking.setNumberOfTables(request.getNumberOfTables());
        booking.setNumberOfReserveTables(request.getNumberOfReserveTables());
        booking.setBookingMode(request.getBookingMode());
        booking.setWeddingPackage(newPackage);
        booking.setSelectedMenuCombo(newSelectedCombo);
        booking.setHallPrice(calculateHallPrice(newHall, newShift, request.getBookingDate()));
        booking.setSubtotalAmount(amountSummary.subtotalAmount);
        booking.setTaxAmount(amountSummary.taxAmount);
        booking.setBookingAmount(amountSummary.bookingAmount);
        booking.setDepositAmount(deposit);
        booking.setConfirmedPaymentAmount(confirmedPaid);
        booking.setRemainingAmount(round(amountSummary.bookingAmount - confirmedPaid));
        booking.setNote(request.getNote());
        booking.setUpdatedBy(currentUserId);
        booking.setUpdatedAt(Instant.now());

        Booking updatedBooking = bookingRepository.save(booking);

        lineRepository.deleteByBookingId(updatedBooking.getId());
        saveLineSnapshots(updatedBooking, draftLines, currentUserId);
        packageSnapshotRepository.deleteByBookingId(updatedBooking.getId());
        savePackageSnapshot(updatedBooking, newPackage, newSelectedCombo, currentUserId);

        if (slotChanged) {
            convertHold(newHall.getId(), newShift.getId(), newBookingDate, currentUserId, updatedBooking);
        }

        bookingDocumentService.regenerateConfirmationDocument(updatedBooking.getId());
        saveAuditLog(currentUserId, "UPDATE_BOOKING", updatedBooking.getId(), updatedBooking.getCustomerName());

        return mapToBookingResponse(updatedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> searchBookings(BookingSearchCriteria criteria) {
        List<Booking> bookings = bookingRepository.findAllActive();

        if (criteria.getCustomerName() != null && !criteria.getCustomerName().isBlank()) {
            String kw = criteria.getCustomerName().toLowerCase();
            bookings = bookings.stream().filter(b -> b.getCustomerName().toLowerCase().contains(kw)).collect(Collectors.toList());
        }
        if (criteria.getCustomerPhone() != null && !criteria.getCustomerPhone().isBlank()) {
            bookings = bookings.stream().filter(b -> b.getCustomerPhone() != null && b.getCustomerPhone().contains(criteria.getCustomerPhone())).collect(Collectors.toList());
        }
        if (criteria.getCustomerEmail() != null && !criteria.getCustomerEmail().isBlank()) {
            String kw = criteria.getCustomerEmail().toLowerCase();
            bookings = bookings.stream().filter(b -> b.getCustomerEmail() != null && b.getCustomerEmail().toLowerCase().contains(kw)).collect(Collectors.toList());
        }
        if (criteria.getBrideName() != null && !criteria.getBrideName().isBlank()) {
            String kw = criteria.getBrideName().toLowerCase();
            bookings = bookings.stream().filter(b -> b.getBrideName().toLowerCase().contains(kw)).collect(Collectors.toList());
        }
        if (criteria.getGroomName() != null && !criteria.getGroomName().isBlank()) {
            String kw = criteria.getGroomName().toLowerCase();
            bookings = bookings.stream().filter(b -> b.getGroomName().toLowerCase().contains(kw)).collect(Collectors.toList());
        }
        if (criteria.getBookingDateFrom() != null) bookings = bookings.stream().filter(b -> !b.getBookingDate().isBefore(toStartOfDay(criteria.getBookingDateFrom()))).collect(Collectors.toList());
        if (criteria.getBookingDateTo() != null) bookings = bookings.stream().filter(b -> !b.getBookingDate().isAfter(toStartOfDay(criteria.getBookingDateTo()))).collect(Collectors.toList());
        if (criteria.getWeddingDateFrom() != null) bookings = bookings.stream().filter(b -> !b.getWeddingDate().isBefore(toStartOfDay(criteria.getWeddingDateFrom()))).collect(Collectors.toList());
        if (criteria.getWeddingDateTo() != null) bookings = bookings.stream().filter(b -> !b.getWeddingDate().isAfter(toStartOfDay(criteria.getWeddingDateTo()))).collect(Collectors.toList());
        if (criteria.getHallId() != null) bookings = bookings.stream().filter(b -> b.getHall().getId().equals(criteria.getHallId())).collect(Collectors.toList());
        if (criteria.getShiftId() != null) bookings = bookings.stream().filter(b -> b.getShift().getId().equals(criteria.getShiftId())).collect(Collectors.toList());
        if (criteria.getStatus() != null) bookings = bookings.stream().filter(b -> b.getStatus() == criteria.getStatus()).collect(Collectors.toList());

        return bookings.stream().map(this::mapToBookingResponse).collect(Collectors.toList());
    }

    @Override
    public CancelBookingResponse cancelBooking(UUID bookingId, String reason, String currentUserId) {
        // BR-CAB-3
        if (reason == null || reason.isBlank()) throw new BadRequestException("MSG2: Lý do hủy không được để trống");

        Booking booking = loadExistingBooking(bookingId);
        // BR-CAB-4
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("MSG67: Trạng thái booking không cho phép hủy");
        }

        long daysBeforeWedding = ChronoUnit.DAYS.between(LocalDate.now(), toLocalDate(booking.getWeddingDate()));
        double totalPaid = bookingPaymentReader.getTotalPaidAmount(bookingId);
        double refundable = bookingPaymentReader.getRefundableAmount(bookingId, daysBeforeWedding);
        double nonRefundable = round(totalPaid - refundable);

        // BR-CAB-5 / BR-CAB-6 / BR-CAB-7
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelReason(reason);
        booking.setCancelledBy(currentUserId);
        booking.setCancelledAt(Instant.now());
        booking.setUpdatedBy(currentUserId);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);

        bookingDocumentService.generateCancellationDocument(bookingId);
        saveAuditLog(currentUserId, "CANCEL_BOOKING", bookingId, booking.getCustomerName());

        return CancelBookingResponse.builder()
                .bookingId(bookingId)
                .customerName(booking.getCustomerName())
                .totalPaidAmount(totalPaid)
                .totalRefundAmount(refundable)
                .nonRefundableAmount(nonRefundable)
                .reason(reason)
                .build();
    }

    @Override
    public BookingResponse updateDishLines(UUID bookingId, EditBookingLinesRequest request, String currentUserId) {
        return replaceItemLines(bookingId, request, currentUserId, BookingLineItemType.DISH, "UPDATE_BOOKING_DISH_LINES");
    }

    @Override
    public BookingResponse updateServiceLines(UUID bookingId, EditBookingLinesRequest request, String currentUserId) {
        return replaceItemLines(bookingId, request, currentUserId, BookingLineItemType.SERVICE, "UPDATE_BOOKING_SERVICE_LINES");
    }

    @Override
    public BookingResponse updateBeverageLines(UUID bookingId, EditBookingLinesRequest request, String currentUserId) {
        return replaceItemLines(bookingId, request, currentUserId, BookingLineItemType.BEVERAGE, "UPDATE_BOOKING_BEVERAGE_LINES");
    }

    private BookingResponse replaceItemLines(UUID bookingId, EditBookingLinesRequest request, String currentUserId, BookingLineItemType itemType, String action) {
        Booking booking = loadExistingBooking(bookingId);
        ensureBookingEditableForLineEdit(booking);

        if (request.getLines() == null) throw new BadRequestException("MSG2: Danh sách dòng không được để trống");
        List<BookingLineRequest> normalized = request.getLines().stream()
                .peek(l -> l.setItemType(itemType))
                .map(this::normalizeLine)
                .collect(Collectors.toList());

        validateLineList(normalized, itemType);

        // Preserve non-target lines, replace target item lines, then recalculate all.
        List<BookingLineRequest> allLines = lineRepository.findByBookingId(bookingId).stream()
                .filter(l -> l.getItemType() != itemType)
                .map(this::toLineRequest)
                .collect(Collectors.toList());
        allLines.addAll(normalized);
        reassignDisplayOrder(allLines);

        AmountSummary amountSummary = recalculateAmount(allLines);
        double confirmedPaid = bookingPaymentReader.getConfirmedPaymentAmountByBooking(bookingId);

        lineRepository.deleteByBookingId(bookingId);
        saveLineSnapshots(booking, allLines, currentUserId);

        booking.setSubtotalAmount(amountSummary.subtotalAmount);
        booking.setTaxAmount(amountSummary.taxAmount);
        booking.setBookingAmount(amountSummary.bookingAmount);
        booking.setConfirmedPaymentAmount(confirmedPaid);
        booking.setRemainingAmount(round(amountSummary.bookingAmount - confirmedPaid));
        booking.setUpdatedBy(currentUserId);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);

        saveAuditLog(currentUserId, action, bookingId, booking.getCustomerName());
        return mapToBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAllActive().stream().map(this::mapToBookingResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID bookingId) {
        return mapToBookingResponse(loadExistingBooking(bookingId));
    }

    private void validateBookingInput(BookingRequest request, boolean create) {
        if (request.getBookingDate() == null || request.getShiftId() == null || request.getHallId() == null) throw new BadRequestException("MSG2: Ngày, ca và sảnh không được để trống");
        if (isBlank(request.getCustomerName())) throw new BadRequestException("MSG2: Tên khách hàng không được để trống");
        if (isBlank(request.getCustomerPhone())) throw new BadRequestException("MSG2: Số điện thoại không được để trống");
        if (!PHONE_PATTERN.matcher(request.getCustomerPhone()).matches()) throw new BadRequestException("MSG30: Số điện thoại không hợp lệ");
        if (request.getCustomerEmail() != null && !request.getCustomerEmail().isBlank() && !EMAIL_PATTERN.matcher(request.getCustomerEmail()).matches()) throw new BadRequestException("MSG31: Email không hợp lệ");
        if (isBlank(request.getBrideName())) throw new BadRequestException("MSG2: Tên cô dâu không được để trống");
        if (isBlank(request.getGroomName())) throw new BadRequestException("MSG2: Tên chú rể không được để trống");
        if (request.getWeddingDate() == null) throw new BadRequestException("MSG2: Ngày cưới không được để trống");
        if (request.getBookingMode() == null) throw new BadRequestException("MSG2: Chế độ đặt tiệc không được để trống");
        if (request.getBookingMode() == BookingMode.PACKAGE && request.getPackageId() == null) throw new BadRequestException("MSG2: Gói tiệc không được để trống");
        if (request.getBookingMode() == BookingMode.MANUAL && (request.getBookingDraftLines() == null || request.getBookingDraftLines().isEmpty())) throw new BadRequestException("MSG2: Danh sách món/dịch vụ không được để trống");
    }

    private void validateTableInput(Integer tables, Integer reserveTables, Hall hall) {
        if (tables == null || tables < hall.getMinTables()) throw new BadRequestException("MSG59: Số bàn phải lớn hơn hoặc bằng số bàn tối thiểu");
        if (tables > hall.getMaxTables()) throw new BadRequestException("MSG66: Số bàn không được vượt quá số bàn tối đa");
        if (reserveTables == null || reserveTables < 0) throw new BadRequestException("MSG24: Số bàn dự phòng phải lớn hơn hoặc bằng 0");
        if (tables + reserveTables > hall.getMaxTables()) throw new BadRequestException("MSG59: Tổng số bàn và bàn dự phòng không được vượt quá sức chứa sảnh");
    }

    private void ensureHallSlotAvailable(UUID hallId, Instant bookingDate, UUID shiftId, UUID excludeBookingId) {
        List<BookingStatus> blockingStatuses = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.ONGOING);
        long conflict = bookingRepository.countSlotConflict(hallId, bookingDate, shiftId, blockingStatuses, excludeBookingId);
        if (conflict > 0) throw new BadRequestException("MSG35: Sảnh đã được đặt hoặc đang được giữ");
    }

    private void validateOrCreateHold(Hall hall, Shift shift, Instant bookingDate, String currentUserId) {
        Instant now = Instant.now();
        List<BookingHallHold> holds = holdRepository.findActiveHoldForSlot(hall.getId(), shift.getId(), bookingDate, BookingHoldStatus.TEMPORARY, now);
        boolean heldByCurrentUser = holds.stream().anyMatch(h -> h.getHeldBy().equals(currentUserId));
        if (!holds.isEmpty() && !heldByCurrentUser) throw new BadRequestException("MSG35: Sảnh đang được giữ bởi người khác");
        if (holds.isEmpty()) {
            BookingHallHold hold = BookingHallHold.builder()
                    .hall(hall)
                    .shift(shift)
                    .bookingDate(bookingDate)
                    .heldBy(currentUserId)
                    .status(BookingHoldStatus.TEMPORARY)
                    .expiredAt(now.plus(HOLD_DURATION_MINUTES, ChronoUnit.MINUTES))
                    .createdBy(currentUserId)
                    .createdAt(now)
                    .isDeleted(false)
                    .build();
            holdRepository.save(hold);
        }
    }

    private void convertHold(UUID hallId, UUID shiftId, Instant bookingDate, String currentUserId, Booking booking) {
        holdRepository.findValidHoldByUser(hallId, shiftId, bookingDate, currentUserId, BookingHoldStatus.TEMPORARY, Instant.now())
                .ifPresent(h -> {
                    h.setStatus(BookingHoldStatus.CONVERTED);
                    h.setBooking(booking);
                    h.setUpdatedBy(currentUserId);
                    h.setUpdatedAt(Instant.now());
                    holdRepository.save(h);
                });
    }

    private List<BookingLineRequest> initializeDraftLines(BookingRequest request, WeddingPackage weddingPackage, DishCombo selectedMenuCombo, Hall hall, double hallPrice) {
        List<BookingLineRequest> lines = new ArrayList<>();
        lines.add(systemHallLine(hall, hallPrice));

        if (request.getBookingMode() == BookingMode.PACKAGE && weddingPackage != null) {
            // selected menu combo -> dish-level lines
            if (selectedMenuCombo != null) {
                for (DishComboSlot slot : dishComboSlotRepository.findByComboId(selectedMenuCombo.getId())) {
                    Dish dish = slot.getDefaultDish();
                    lines.add(toLine(dish.getId(), dish.getName(), BookingLineItemType.DISH, 1, dish.getUnitPrice(), BookingLineSourceType.PACKAGE_INCLUDED, selectedMenuCombo.getId(), selectedMenuCombo.getName(), false, false));
                }
            }

            for (WeddingPackageServiceItem item : packageServiceItemRepository.findByPackageId(weddingPackage.getId())) {
                com.wedding.management.domain.service.model.Service service = serviceRepository.findByIdAndIsDeletedFalse(item.getServiceId()).orElse(null);
                double price = service == null ? 0.0 : service.getPrice();
                String name = service == null ? item.getServiceName() : service.getName();
                lines.add(toLine(item.getServiceId(), name, BookingLineItemType.SERVICE, item.getQuantity() == null ? 1 : item.getQuantity(), price, BookingLineSourceType.PACKAGE_INCLUDED, weddingPackage.getId(), weddingPackage.getName(), false, false));
            }

            for (WeddingPackageBeverageAllowance allowance : packageBeverageAllowanceRepository.findByPackageId(weddingPackage.getId())) {
                Beverage beverage = allowance.getBeverage();
                lines.add(toLine(beverage.getId(), beverage.getName(), BookingLineItemType.BEVERAGE, allowance.getAllowanceQuantity(), beverage.getUnitPrice(), BookingLineSourceType.PACKAGE_INCLUDED, weddingPackage.getId(), weddingPackage.getName(), false, false));
            }

            int benefitOrder = 1;
            for (WeddingPackageBenefit benefit : packageBenefitRepository.findByPackageId(weddingPackage.getId())) {
                BookingLineRequest benefitLine = toLine(null, benefit.getBenefitDescription(), BookingLineItemType.BENEFIT, 1, 0.0, BookingLineSourceType.PACKAGE_BENEFIT, weddingPackage.getId(), weddingPackage.getName(), false, false);
                benefitLine.setDisplayOrder(benefitOrder++);
                lines.add(benefitLine);
            }
        }

        if (request.getBookingDraftLines() != null) {
            for (BookingLineRequest line : request.getBookingDraftLines()) {
                if (line.getItemType() != BookingLineItemType.HALL) lines.add(normalizeLine(line));
            }
        }

        reassignDisplayOrder(lines);
        return lines;
    }

    private BookingLineRequest normalizeLine(BookingLineRequest input) {
        if (input.getItemType() == null) throw new BadRequestException("MSG2: Loại dòng không được để trống");
        if (input.getQuantity() == null || input.getQuantity() <= 0) throw new BadRequestException("MSG2: Số lượng phải lớn hơn 0");

        if (input.getItemType() == BookingLineItemType.DISH && input.getItemId() != null) {
            Dish dish = dishRepository.findByIdAndIsDeletedFalse(input.getItemId()).orElseThrow(() -> new ResourceNotFoundException("Món ăn không tồn tại"));
            if (dish.getStatus() != DishStatus.ACTIVE) throw new BadRequestException("MSG2: Món ăn không hoạt động");
            input.setItemName(dish.getName());
            input.setUnitPrice(input.getUnitPrice() == null ? dish.getUnitPrice() : input.getUnitPrice());
        }
        if (input.getItemType() == BookingLineItemType.SERVICE && input.getItemId() != null) {
            com.wedding.management.domain.service.model.Service service = serviceRepository.findByIdAndIsDeletedFalse(input.getItemId()).orElseThrow(() -> new ResourceNotFoundException("Dịch vụ không tồn tại"));
            if (service.getStatus() != ServiceStatus.ACTIVE) throw new BadRequestException("MSG2: Dịch vụ không hoạt động");
            input.setItemName(service.getName());
            input.setUnitPrice(input.getUnitPrice() == null ? service.getPrice() : input.getUnitPrice());
        }
        if (input.getItemType() == BookingLineItemType.BEVERAGE && input.getItemId() != null) {
            Beverage beverage = beverageRepository.findByIdAndIsDeletedFalse(input.getItemId()).orElseThrow(() -> new ResourceNotFoundException("Đồ uống không tồn tại"));
            if (beverage.getStatus() != BeverageStatus.ACTIVE) throw new BadRequestException("MSG2: Đồ uống không hoạt động");
            input.setItemName(beverage.getName());
            input.setUnitPrice(input.getUnitPrice() == null ? beverage.getUnitPrice() : input.getUnitPrice());
        }

        if (isBlank(input.getItemName())) throw new BadRequestException("MSG2: Tên dòng không được để trống");
        if (input.getUnitPrice() == null || input.getUnitPrice() < 0) input.setUnitPrice(0.0);
        if (input.getDiscountAmount() == null || input.getDiscountAmount() < 0) input.setDiscountAmount(0.0);
        if (input.getTaxRate() == null || input.getTaxRate() < 0) input.setTaxRate(DEFAULT_TAX_RATE);
        if (input.getSourceType() == null) input.setSourceType(BookingLineSourceType.MANUAL_EXTRA);
        if (input.getEditable() == null) input.setEditable(true);
        if (input.getRemovable() == null) input.setRemovable(true);
        return input;
    }

    private void validateLineList(List<BookingLineRequest> lines, BookingLineItemType expectedType) {
        for (BookingLineRequest line : lines) {
            if (line.getItemType() != expectedType) throw new BadRequestException("MSG2: Danh sách dòng không hợp lệ");
            if (line.getQuantity() == null || line.getQuantity() <= 0) throw new BadRequestException("MSG2: Số lượng phải lớn hơn 0");
            if (line.getUnitPrice() == null || line.getUnitPrice() < 0) throw new BadRequestException("MSG13: Giá phải lớn hơn hoặc bằng 0");
        }
    }

    private AmountSummary recalculateAmount(List<BookingLineRequest> lines) {
        double subtotal = 0.0;
        double tax = 0.0;
        for (BookingLineRequest line : lines) {
            line = normalizeLine(line);
            double gross = (line.getUnitPrice() * line.getQuantity()) - line.getDiscountAmount();
            gross = Math.max(gross, 0.0);
            double lineTax = gross * line.getTaxRate();
            subtotal += gross;
            tax += lineTax;
        }
        return new AmountSummary(round(subtotal), round(tax), round(subtotal + tax));
    }

    private void saveLineSnapshots(Booking booking, List<BookingLineRequest> lines, String currentUserId) {
        int order = 1;
        for (BookingLineRequest request : lines) {
            request = normalizeLine(request);
            double gross = Math.max((request.getUnitPrice() * request.getQuantity()) - request.getDiscountAmount(), 0.0);
            double lineTax = round(gross * request.getTaxRate());
            BookingLineSnapshot snapshot = BookingLineSnapshot.builder()
                    .booking(booking)
                    .itemType(request.getItemType())
                    .itemId(request.getItemId())
                    .itemName(request.getItemName())
                    .quantity(request.getQuantity())
                    .unitPrice(request.getUnitPrice())
                    .discountAmount(request.getDiscountAmount())
                    .taxRate(request.getTaxRate())
                    .taxAmount(lineTax)
                    .lineAmount(round(gross + lineTax))
                    .sourceType(request.getSourceType())
                    .sourceId(request.getSourceId())
                    .sourceName(request.getSourceName())
                    .editable(request.getEditable())
                    .removable(request.getRemovable())
                    .displayOrder(request.getDisplayOrder() == null ? order : request.getDisplayOrder())
                    .createdBy(currentUserId)
                    .createdAt(Instant.now())
                    .isDeleted(false)
                    .build();
            lineRepository.save(snapshot);
            order++;
        }
    }

    private void savePackageSnapshot(Booking booking, WeddingPackage weddingPackage, DishCombo selectedMenuCombo, String currentUserId) {
        if (weddingPackage == null) return;
        BookingPackageSnapshot snapshot = BookingPackageSnapshot.builder()
                .booking(booking)
                .packageId(weddingPackage.getId())
                .packageName(weddingPackage.getName())
                .packageDescription(weddingPackage.getDescription())
                .packagePolicySnapshot(buildPackagePolicySnapshot(weddingPackage))
                .selectedMenuComboId(selectedMenuCombo == null ? null : selectedMenuCombo.getId())
                .selectedMenuComboName(selectedMenuCombo == null ? null : selectedMenuCombo.getName())
                .createdBy(currentUserId)
                .createdAt(Instant.now())
                .isDeleted(false)
                .build();
        packageSnapshotRepository.save(snapshot);
    }

    private String buildPackagePolicySnapshot(WeddingPackage weddingPackage) {
        return "WeddingPackageSnapshot{name='" + weddingPackage.getName() + "', status='" + weddingPackage.getStatus() + "'}";
    }

    private BookingLineRequest systemHallLine(Hall hall, double hallPrice) {
        return toLine(hall.getId(), hall.getName(), BookingLineItemType.HALL, 1, hallPrice, BookingLineSourceType.SYSTEM, hall.getId(), "HALL", false, false);
    }

    private BookingLineRequest toLine(UUID itemId, String itemName, BookingLineItemType type, Integer quantity, Double unitPrice, BookingLineSourceType sourceType, UUID sourceId, String sourceName, Boolean editable, Boolean removable) {
        BookingLineRequest line = new BookingLineRequest();
        line.setItemType(type);
        line.setItemId(itemId);
        line.setItemName(itemName);
        line.setQuantity(quantity == null ? 1 : quantity);
        line.setUnitPrice(unitPrice == null ? 0.0 : unitPrice);
        line.setDiscountAmount(0.0);
        line.setTaxRate(DEFAULT_TAX_RATE);
        line.setSourceType(sourceType);
        line.setSourceId(sourceId);
        line.setSourceName(sourceName);
        line.setEditable(editable);
        line.setRemovable(removable);
        return line;
    }

    private BookingLineRequest toLineRequest(BookingLineSnapshot snapshot) {
        BookingLineRequest request = new BookingLineRequest();
        request.setItemType(snapshot.getItemType());
        request.setItemId(snapshot.getItemId());
        request.setItemName(snapshot.getItemName());
        request.setQuantity(snapshot.getQuantity());
        request.setUnitPrice(snapshot.getUnitPrice());
        request.setDiscountAmount(snapshot.getDiscountAmount());
        request.setTaxRate(snapshot.getTaxRate());
        request.setSourceType(snapshot.getSourceType());
        request.setSourceId(snapshot.getSourceId());
        request.setSourceName(snapshot.getSourceName());
        request.setEditable(snapshot.getEditable());
        request.setRemovable(snapshot.getRemovable());
        request.setDisplayOrder(snapshot.getDisplayOrder());
        return request;
    }

    private void ensureBookingEditableForLineEdit(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING) throw new BadRequestException("MSG67: Trạng thái booking không cho phép chỉnh sửa danh sách món/dịch vụ/đồ uống");
    }

    private Hall loadActiveHall(UUID hallId) {
        Hall hall = hallRepository.findByIdAndIsDeletedFalse(hallId).orElseThrow(() -> new ResourceNotFoundException("Sảnh không tồn tại"));
        if (hall.getStatus() != HallStatus.ACTIVE) throw new BadRequestException("MSG2: Sảnh không hoạt động");
        return hall;
    }

    private Shift loadActiveShift(UUID shiftId) {
        Shift shift = shiftRepository.findByIdAndIsDeletedFalse(shiftId).orElseThrow(() -> new ResourceNotFoundException("Ca không tồn tại"));
        if (shift.getStatus() != ShiftStatus.ACTIVE) throw new BadRequestException("MSG2: Ca không hoạt động");
        return shift;
    }

    private WeddingPackage loadActiveWeddingPackage(UUID packageId) {
        WeddingPackage weddingPackage = weddingPackageRepository.findByIdAndIsDeletedFalse(packageId).orElseThrow(() -> new ResourceNotFoundException("Gói tiệc không tồn tại"));
        if (weddingPackage.getStatus() != WeddingPackageStatus.ACTIVE) throw new BadRequestException("MSG2: Gói tiệc không hoạt động");
        return weddingPackage;
    }

    private DishCombo resolveSelectedMenuCombo(WeddingPackage weddingPackage, UUID selectedMenuComboId) {
        List<WeddingPackageMenuCombo> options = packageMenuComboRepository.findByPackageId(weddingPackage.getId());
        if (options.isEmpty()) return weddingPackage.getDefaultMenuCombo();
        UUID selected = selectedMenuComboId == null ? weddingPackage.getDefaultMenuCombo().getId() : selectedMenuComboId;
        boolean allowed = options.stream().anyMatch(o -> o.getDishCombo().getId().equals(selected));
        if (!allowed) throw new BadRequestException("MSG2: Menu combo không thuộc gói tiệc đã chọn");
        DishCombo combo = dishComboRepository.findByIdAndIsDeletedFalse(selected).orElseThrow(() -> new ResourceNotFoundException("Menu combo không tồn tại"));
        if (combo.getStatus() != DishComboStatus.ACTIVE) throw new BadRequestException("MSG2: Menu combo không hoạt động");
        return combo;
    }

    private boolean detectPackageChange(Booking booking, BookingMode newMode, WeddingPackage newPackage, DishCombo newCombo) {
        UUID oldPackageId = booking.getWeddingPackage() == null ? null : booking.getWeddingPackage().getId();
        UUID newPackageId = newPackage == null ? null : newPackage.getId();
        UUID oldComboId = booking.getSelectedMenuCombo() == null ? null : booking.getSelectedMenuCombo().getId();
        UUID newComboId = newCombo == null ? null : newCombo.getId();
        return booking.getBookingMode() != newMode || !Objects.equals(oldPackageId, newPackageId) || !Objects.equals(oldComboId, newComboId);
    }

    private Booking loadExistingBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking không tồn tại"));
        if (Boolean.TRUE.equals(booking.getIsDeleted()) || booking.getStatus() == BookingStatus.DELETED) throw new ResourceNotFoundException("Booking đã bị xóa");
        return booking;
    }

    private double calculateHallPrice(Hall hall, Shift shift, LocalDate bookingDate) {
        TimeSlot timeSlot = resolveTimeSlot(shift);
        DayType dayType = isWeekend(bookingDate) ? DayType.WEEKEND : DayType.WEEKDAY;
        return hallPricingRepository.findByHallIdAndTimeSlotAndDayType(hall.getId(), timeSlot, dayType)
                .map(HallPricing::getPrice)
                .orElse(hall.getHallType().getBasePrice());
    }

    private TimeSlot resolveTimeSlot(Shift shift) {
        String name = shift.getName() == null ? "" : shift.getName().toLowerCase();
        if (name.contains("morning") || name.contains("sáng")) return TimeSlot.MORNING;
        if (name.contains("afternoon") || name.contains("chiều")) return TimeSlot.AFTERNOON;
        if (name.contains("evening") || name.contains("tối")) return TimeSlot.EVENING;
        int hour = shift.getStartTime() == null ? 18 : shift.getStartTime().getHour();
        if (hour < 12) return TimeSlot.MORNING;
        if (hour < 17) return TimeSlot.AFTERNOON;
        return TimeSlot.EVENING;
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        List<BookingLineResponse> lines = lineRepository.findByBookingId(booking.getId()).stream()
                .map(this::mapLine)
                .collect(Collectors.toList());
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingDate(toLocalDate(booking.getBookingDate()))
                .shiftId(booking.getShift().getId())
                .shiftName(booking.getShift().getName())
                .hallId(booking.getHall().getId())
                .hallName(booking.getHall().getName())
                .hallTypeName(booking.getHall().getHallType().getName())
                .customerName(booking.getCustomerName())
                .customerPhone(booking.getCustomerPhone())
                .customerEmail(booking.getCustomerEmail())
                .brideName(booking.getBrideName())
                .groomName(booking.getGroomName())
                .weddingDate(toLocalDate(booking.getWeddingDate()))
                .numberOfTables(booking.getNumberOfTables())
                .numberOfReserveTables(booking.getNumberOfReserveTables())
                .bookingMode(booking.getBookingMode())
                .packageId(booking.getWeddingPackage() == null ? null : booking.getWeddingPackage().getId())
                .packageName(booking.getWeddingPackage() == null ? null : booking.getWeddingPackage().getName())
                .selectedMenuComboId(booking.getSelectedMenuCombo() == null ? null : booking.getSelectedMenuCombo().getId())
                .selectedMenuComboName(booking.getSelectedMenuCombo() == null ? null : booking.getSelectedMenuCombo().getName())
                .hallPrice(booking.getHallPrice())
                .subtotalAmount(booking.getSubtotalAmount())
                .taxAmount(booking.getTaxAmount())
                .bookingAmount(booking.getBookingAmount())
                .depositAmount(booking.getDepositAmount())
                .confirmedPaymentAmount(booking.getConfirmedPaymentAmount())
                .remainingAmount(booking.getRemainingAmount())
                .note(booking.getNote())
                .status(booking.getStatus())
                .cancelReason(booking.getCancelReason())
                .bookingLines(lines)
                .lastModifiedAt(booking.getUpdatedAt())
                .lastModifiedBy(booking.getUpdatedBy())
                .build();
    }

    private BookingLineResponse mapLine(BookingLineSnapshot line) {
        return BookingLineResponse.builder()
                .id(line.getId())
                .itemType(line.getItemType())
                .itemId(line.getItemId())
                .itemName(line.getItemName())
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .discountAmount(line.getDiscountAmount())
                .taxRate(line.getTaxRate())
                .taxAmount(line.getTaxAmount())
                .lineAmount(line.getLineAmount())
                .sourceType(line.getSourceType())
                .sourceId(line.getSourceId())
                .sourceName(line.getSourceName())
                .editable(line.getEditable())
                .removable(line.getRemovable())
                .displayOrder(line.getDisplayOrder())
                .build();
    }

    private void reassignDisplayOrder(List<BookingLineRequest> lines) {
        int i = 1;
        for (BookingLineRequest line : lines) line.setDisplayOrder(i++);
    }

    private Instant toStartOfDay(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private LocalDate toLocalDate(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private void saveAuditLog(String userId, String action, UUID targetId, String targetName) {
        try {
            UUID userUUID = UUID.fromString(userId);
            AuditLog auditLog = AuditLog.builder()
                    .userId(userUUID)
                    .action(action)
                    .targetId(targetId)
                    .targetName(targetName)
                    .createdAt(Instant.now())
                    .build();
            auditLogRepository.save(auditLog);
        } catch (IllegalArgumentException e) {
            // Keep same style as existing Hall/HallType code: skip audit if principal name is not UUID.
        }
    }

    private record AmountSummary(double subtotalAmount, double taxAmount, double bookingAmount) {}
}
