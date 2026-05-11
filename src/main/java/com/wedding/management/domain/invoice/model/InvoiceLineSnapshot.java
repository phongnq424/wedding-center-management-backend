package com.wedding.management.domain.invoice.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.booking.enums.BookingLineItemType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "invoice_line_snapshots", indexes = {
        @Index(name = "idx_invoice_line_invoice", columnList = "invoice_id"),
        @Index(name = "idx_invoice_line_item_type", columnList = "item_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Accessors(chain = true)
public class InvoiceLineSnapshot extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private BookingLineItemType itemType;

    private UUID itemId;
    @Column(nullable = false)
    private String itemName;
    @Column(nullable = false)
    private Integer quantity;
    @Column(nullable = false)
    private Double unitPrice;
    @Column(nullable = false)
    private Double discountAmount;
    @Column(nullable = false)
    private Double taxRate;
    @Column(nullable = false)
    private Double taxAmount;
    @Column(nullable = false)
    private Double lineAmount;
    @Column(nullable = false)
    private Integer displayOrder;
}
