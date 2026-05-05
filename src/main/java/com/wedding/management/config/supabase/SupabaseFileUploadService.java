package com.wedding.management.config.supabase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupabaseFileUploadService {

    private final SupabaseConfig supabaseConfig;
    private final SupabaseHttpClient supabaseHttpClient;

    private static final String IMAGE_PATH_TEMPLATE = "%s/storage/v1/object/public/%s/%s/%s";
    private static final Pattern VALID_IMAGE_TYPES = Pattern.compile("^image/(jpeg|png|gif|webp|jpg)$");

    /**
     * Upload file to Supabase public bucket
     * 
     * @param file   MultipartFile to upload
     * @param folder Folder path (halls, dishes, services, beverages)
     * @return Public URL of uploaded file
     */
    public String uploadPublicFile(MultipartFile file, String folder) throws IOException {
        validateFile(file);
        return uploadFile(file, folder, supabaseConfig.getPublicBucket());
    }

    /**
     * Upload file to Supabase private bucket
     * 
     * @param file   MultipartFile to upload
     * @param folder Folder path (for organization)
     * @return File path in private bucket
     */
    public String uploadPrivateFile(MultipartFile file, String folder) throws IOException {
        validateFile(file);
        return uploadFile(file, folder, supabaseConfig.getPrivateBucket());
    }

    /**
     * Delete file from Supabase
     * 
     * @param bucket   Bucket name
     * @param filePath File path including folder
     */
    public void deleteFile(String bucket, String filePath) {
        supabaseHttpClient.deleteFile(bucket, filePath);
    }

    /**
     * Get public URL for a file in public bucket
     */
    public String getPublicFileUrl(String folder, String filename) {
        return String.format(
                IMAGE_PATH_TEMPLATE,
                supabaseConfig.getSupabaseUrl(),
                supabaseConfig.getPublicBucket(),
                folder,
                filename);
    }

    // ======================== Private Methods ========================

    private String uploadFile(MultipartFile file, String folder, String bucket) throws IOException {
        String filename = generateUniqueFilename(file.getOriginalFilename());
        String filePath = folder + "/" + filename;

        byte[] fileContent = file.getBytes();
        supabaseHttpClient.uploadFile(bucket, filePath, fileContent, file.getContentType());

        log.info("File uploaded successfully: bucket={}, path={}", bucket, filePath);

        // If public bucket, return full URL; if private, return path
        if (bucket.equals(supabaseConfig.getPublicBucket())) {
            return getPublicFileUrl(folder, filename);
        } else {
            return filePath;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        String contentType = file.getContentType();
        if (contentType == null || !VALID_IMAGE_TYPES.matcher(contentType).matches()) {
            throw new IllegalArgumentException("Chỉ hỗ trợ các định dạng hình ảnh (JPEG, PNG, GIF, WebP)");
        }

        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 5MB");
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID() + "." + extension;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
