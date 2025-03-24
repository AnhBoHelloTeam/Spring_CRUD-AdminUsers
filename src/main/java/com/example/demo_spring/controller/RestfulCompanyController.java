    package com.example.demo_spring.controller;

    import com.example.demo_spring.model.Company;
    import com.example.demo_spring.model.User;
    import com.example.demo_spring.service.CompanyService;
    import com.example.demo_spring.service.UserService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;
    import java.util.Map;
    import java.util.Optional;
    import java.util.stream.Collectors;

    @RestController
    @RequestMapping("/api/v1/companies")
    @CrossOrigin(origins = "*")
    public class RestfulCompanyController {

        @Autowired
        private CompanyService companyService;

        @Autowired
        private UserService userService;

        @PostMapping
        public ResponseEntity<Company> saveCompany(@RequestBody Company company) {
            companyService.saveCompany(company);
            return ResponseEntity.ok(company);
        }

        @GetMapping
        public ResponseEntity<List<Company>> listCompanies() {
            List<Company> companies = companyService.getAllCompanies();
            return ResponseEntity.ok(companies);
        }

        @GetMapping("/{companyId}/users")
        public ResponseEntity<List<User>> listUsersByCompany(@PathVariable int companyId) {
            Optional<Company> company = companyService.getCompanyById(companyId);
            if (company.isPresent()) {
                return ResponseEntity.ok(companyService.getUsersByCompany(companyId));
            }
            return ResponseEntity.notFound().build();
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<String> deleteCompany(@PathVariable int id) {
            companyService.deleteCompany(id);
            return ResponseEntity.ok("Company deleted successfully");
        }

        @GetMapping("/{companyId}/available-users")
        public ResponseEntity<List<User>> getAvailableUsers(@PathVariable int companyId) {
            List<User> availableUsers = userService.getAllUsers().stream()
                    .filter(user -> user.getCompany() == null || user.getCompany().getId() != companyId)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(availableUsers);
        }
        @PostMapping("/{companyId}/add-users")
        public ResponseEntity<String> addUsersToCompany(@PathVariable int companyId, @RequestBody Map<String, List<Integer>> requestBody) {
            System.out.println("📌 Đã nhận request thêm user vào công ty ID: " + companyId);

            List<Integer> userIds = requestBody.get("userIds");
            if (userIds == null || userIds.isEmpty()) {
                return ResponseEntity.badRequest().body("User list is empty or missing");
            }

            System.out.println("✅ Danh sách userIds: " + userIds);

            Optional<Company> companyOptional = companyService.getCompanyById(companyId);
            if (companyOptional.isPresent()) {
                Company company = companyOptional.get();
                List<User> users = userService.getAllUsers().stream()
                        .filter(user -> userIds.contains(user.getId()))
                        .collect(Collectors.toList());

                for (User user : users) {
                    user.setCompany(company);
                    userService.saveUser(user, companyId);
                }
                System.out.println("✅ Users added to company successfully");
                return ResponseEntity.ok("Users added to company successfully");
            }
            System.out.println("❌ Không tìm thấy công ty ID: " + companyId);
            return ResponseEntity.notFound().build();
        }



        @PostMapping("/remove-user/{userId}")
        public ResponseEntity<String> removeUserFromCompany(@PathVariable int userId) {
            Optional<User> userOptional = userService.getUserById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setCompany(null);
                userService.saveUser(user, null);
                return ResponseEntity.ok("User removed from company successfully");
            }
            return ResponseEntity.notFound().build();
        }
    }