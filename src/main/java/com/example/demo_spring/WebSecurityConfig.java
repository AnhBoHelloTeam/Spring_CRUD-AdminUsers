package com.example.demo_spring;

import com.example.demo_spring.service.UserDetailsServiceIml;
import com.example.demo_spring.JwtAuthenticationFilter;
import com.example.demo_spring.utils.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceIml();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(List.of(provider));
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF cho API RESTful

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/users/register", "/api/v1/users/login").permitAll() // Cho phép đăng ký & đăng nhập không cần auth
                        .requestMatchers("/h2-console/**").permitAll() // Cho phép truy cập H2 Console
                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN") // Chỉ ADMIN mới có quyền quản lý user
                        .requestMatchers("/api/v1/companies/**").hasRole("ADMIN") // Chỉ ADMIN mới có quyền quản lý company
                        .anyRequest().authenticated() // Các request khác cần xác thực
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Thêm filter JWT

        return http.build();
    }
}
