package com.siddhesh.inventoryManagement.config;

import com.siddhesh.inventoryManagement.domain.entities.Role;
import com.siddhesh.inventoryManagement.domain.entities.User;

import com.siddhesh.inventoryManagement.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;

    @Bean
    CommandLineRunner initAdmin() {
        return args -> {

            String adminPhone = "9763369894";

            if (userRepository.findByPhoneNumber(adminPhone).isEmpty()) {

                User admin = new User();

                admin.setPhoneNumber(adminPhone);
                admin.setName("SIMON");
                admin.setEmail("admin@example.com");
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);

                System.out.println("=================================");
                System.out.println("ADMIN USER CREATED");
                System.out.println("Phone: " + adminPhone);
                System.out.println("Role: ADMIN");
                System.out.println("=================================");

            } else {

                System.out.println("ADMIN USER ALREADY EXISTS");
            }
        };
    }
}