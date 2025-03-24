package com.example.demo_spring.service;

import com.example.demo_spring.model.User;
import com.example.demo_spring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceIml implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("🔍 Đang gọi loadUserByUsername() với email: " + email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("🚨 User not found with email: " + email));

        System.out.println("✅ Tìm thấy user: " + user.getEmail());
        System.out.println("🔐 Mật khẩu hash từ DB: " + user.getPassword());
        System.out.println("🎭 Vai trò từ DB: " + user.getRole());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())) // ✅ Đảm bảo role có "ROLE_"
        );
    }
}
