package com.edumy.backend;

import com.edumy.backend.model.*;
import com.edumy.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@edumy.com") != null) {
            return;
        }

        Role adminRole = new Role(); adminRole.setName("Admin");
        Role instructorRole = new Role(); instructorRole.setName("Instructor");
        Role studentRole = new Role(); studentRole.setName("Student");
        roleRepository.saveAll(Arrays.asList(adminRole, instructorRole, studentRole));

        User admin = new User();
        admin.setEmail("admin@edumy.com");
        admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
        admin.setFullName("System Admin");
        admin.setIsActive(true);
        admin.setRole("Admin");
        userRepository.save(admin);

        UserRole ur = new UserRole();
        ur.setUserId(admin.getUserId());
        ur.setRoleId(adminRole.getId());
        userRoleRepository.save(ur);

        Category devCat = new Category(); devCat.setName("Development");
        Category busCat = new Category(); busCat.setName("Business");
        Category designCat = new Category(); designCat.setName("Design");
        Category marketingCat = new Category(); marketingCat.setName("Marketing");
        Category itCat = new Category(); itCat.setName("IT & Software");
        categoryRepository.saveAll(Arrays.asList(devCat, busCat, designCat, marketingCat, itCat));

        System.out.println("Database seeded successfully.");
    }
}
