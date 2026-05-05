package com.wedding.management.config.supabase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class SupabaseConfig {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${supabase.public-bucket}")
    private String publicBucket;

    @Value("${supabase.private-bucket}")
    private String privateBucket;
}
