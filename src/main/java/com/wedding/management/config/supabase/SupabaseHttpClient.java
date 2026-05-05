package com.wedding.management.config.supabase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class SupabaseHttpClient {

    private final RestTemplate restTemplate;
    private final SupabaseConfig supabaseConfig;

    private static final String STORAGE_API_TEMPLATE = "%s/storage/v1/object/%s/%s";

    /**
     * Upload file to Supabase storage
     */
    public void uploadFile(String bucket, String filePath, byte[] fileContent, String contentType) {
        String uploadUrl = String.format(STORAGE_API_TEMPLATE, supabaseConfig.getSupabaseUrl(), bucket, filePath);

        HttpHeaders headers = createHeaders();
        if (contentType != null) {
            headers.setContentType(org.springframework.http.MediaType.parseMediaType(contentType));
        }

        HttpEntity<byte[]> entity = new HttpEntity<>(fileContent, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    entity,
                    String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Không thể upload file lên Supabase: " + response.getStatusCode());
            }

            log.info("File uploaded to Supabase: bucket={}, path={}", bucket, filePath);
        } catch (RestClientException e) {
            log.error("Error uploading file to Supabase: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi upload file: " + e.getMessage(), e);
        }
    }

    /**
     * Delete file from Supabase storage
     */
    public void deleteFile(String bucket, String filePath) {
        String deleteUrl = String.format(STORAGE_API_TEMPLATE, supabaseConfig.getSupabaseUrl(), bucket, filePath);

        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    deleteUrl,
                    HttpMethod.DELETE,
                    entity,
                    Void.class);

            if (!response.getStatusCode().is2xxSuccessful() && response.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw new RuntimeException("Không thể xóa file khỏi Supabase: " + response.getStatusCode());
            }

            log.info("File deleted from Supabase: bucket={}, path={}", bucket, filePath);
        } catch (RestClientException e) {
            log.error("Error deleting file from Supabase: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi xóa file: " + e.getMessage(), e);
        }
    }

    // ======================== Private Methods ========================

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseConfig.getServiceRoleKey());
        headers.set("apikey", supabaseConfig.getServiceRoleKey());
        return headers;
    }
}
