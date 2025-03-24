package com.example.demo_spring.service;

import com.example.demo_spring.model.Company;
import com.example.demo_spring.model.User;
import com.example.demo_spring.repository.CompanyRepository;
import com.example.demo_spring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    // Lấy danh sách tất cả công ty
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    // Lưu hoặc cập nhật công ty
    public void saveCompany(Company company) {
        companyRepository.save(company);
    }

    // Tìm công ty theo ID
    public Optional<Company> getCompanyById(int id) {
        return companyRepository.findById(id);
    }

    // Lấy danh sách user của một công ty
    public List<User> getUsersByCompany(int companyId) {
        Optional<Company> company = companyRepository.findById(companyId);
        return company.map(Company::getUsers).orElse(List.of());
    }

    // Xóa công ty và cập nhật user liên quan
    public void deleteCompany(int id) {
        Optional<Company> companyOptional = companyRepository.findById(id);
        if (companyOptional.isPresent()) {
            Company company = companyOptional.get();

            // Xóa liên kết với user trước khi xóa công ty
            List<User> users = company.getUsers();
            for (User user : users) {
                user.setCompany(null);
                userRepository.save(user);
            }

            companyRepository.delete(company);
        }
    }
}
