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

        User instructor = new User();
        instructor.setEmail("instructor@edumy.com");
        instructor.setPasswordHash(passwordEncoder.encode("Admin@123"));
        instructor.setFullName("Giảng viên Edumy");
        instructor.setIsActive(true);
        instructor.setRole("Instructor");
        userRepository.save(instructor);

        UserRole urInst = new UserRole();
        urInst.setUserId(instructor.getUserId());
        urInst.setRoleId(instructorRole.getId());
        userRoleRepository.save(urInst);

        User student = new User();
        student.setEmail("student@edumy.com");
        student.setPasswordHash(passwordEncoder.encode("Admin@123"));
        student.setFullName("Học viên Edumy");
        student.setIsActive(true);
        student.setRole("Student");
        userRepository.save(student);

        UserRole urStud = new UserRole();
        urStud.setUserId(student.getUserId());
        urStud.setRoleId(studentRole.getId());
        userRoleRepository.save(urStud);

        Category devCat = new Category(); devCat.setName("Development");
        Category busCat = new Category(); busCat.setName("Business");
        Category designCat = new Category(); designCat.setName("Design");
        Category marketingCat = new Category(); marketingCat.setName("Marketing");
        Category itCat = new Category(); itCat.setName("IT & Software");
        categoryRepository.saveAll(Arrays.asList(devCat, busCat, designCat, marketingCat, itCat));

        System.out.println("Database seeded successfully.");
    }
}
