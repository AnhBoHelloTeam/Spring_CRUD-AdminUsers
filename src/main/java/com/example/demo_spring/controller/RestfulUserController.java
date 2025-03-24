package com.example.demo_spring.controller;

import com.example.demo_spring.model.Company;
import com.example.demo_spring.model.User;
import com.example.demo_spring.service.CompanyService;
import com.example.demo_spring.service.UserService;
import com.example.demo_spring.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class RestfulUserController {
    @Autowired
    private AuthenticationManager authenticationManager;

    private final UserService userService;
    private final CompanyService companyService;
    private final JwtUtil jwtUtil;

    public RestfulUserController(UserService userService, CompanyService companyService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.companyService = companyService;
        this.jwtUtil = jwtUtil;
    }
    @PostMapping(value = "/login", consumes = "application/json")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            if (user.getEmail() == null || user.getPassword() == null) {
                return ResponseEntity.badRequest().body("Email và mật khẩu không được để trống.");
            }

            // Kiểm tra xem email có tồn tại trong DB không
            Optional<User> existingUser = userService.getUserByEmail(user.getEmail());
            if (existingUser.isEmpty()) {
                return ResponseEntity.status(404).body("Email không tồn tại trong hệ thống.");
            }

            // Xác thực người dùng
            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
            Authentication authentication = authenticationManager.authenticate(authRequest);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Lấy role từ authentication
            String role = authentication.getAuthorities().iterator().next().getAuthority();

            // Tạo token JWT có role
            String token = jwtUtil.generateToken(user.getEmail(), role);

            return ResponseEntity.ok().body(Map.of(
                    "token", "Bearer " + token,
                    "email", user.getEmail(),
                    "role", role
            ));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            return ResponseEntity.status(401).body("Sai mật khẩu. Vui lòng thử lại.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }



    @PostMapping(value = "/register", consumes = "application/json")
    public ResponseEntity<String> register(@RequestBody User user) {
        try {
            String role = (user.getRole() != null && !user.getRole().isEmpty()) ? user.getRole() : "USER";
            userService.registerUser(user.getName(), user.getClassSchool(), user.getPhone(), user.getEmail(), user.getImgURL(), user.getPassword(), role);
            return ResponseEntity.ok("User registered successfully with role: " + role);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") int id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        userService.addUser(user);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") int id, @RequestBody User updatedUser) {
        boolean isUpdated = userService.updateUser(id, updatedUser);
        if (isUpdated) {
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") int id) {
        boolean isDeleted = userService.deleteUser(id);
        if (isDeleted) {
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @DeleteMapping("/deleteAll")
    public ResponseEntity<String> deleteAllUsers() {
        try {
            userService.deleteAllUsers();
            return ResponseEntity.ok("All users deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting all users: " + e.getMessage());
        }
    }
}
