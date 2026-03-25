package com.wedding.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println(">>> CUSTOM SECURITY CONFIG LOADED");
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập công khai để test các API Hall và Shift
                        .requestMatchers("/api/v1/halls/**").permitAll()
                        .requestMatchers("/api/v1/shifts/**").permitAll()
                        .requestMatchers("/api/v1/services/**").permitAll()

                        .anyRequest().authenticated()
                );
        return http.build();
    }
}