package com.allinoneshop.config;

import com.allinoneshop.entity.User;
import com.allinoneshop.entity.enums.Role;
import com.allinoneshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@shop.com")) {
            userRepository.save(User.builder()
                    .email("admin@shop.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .role(Role.ADMIN)
                    .build());
            log.info("Default admin user created: admin@shop.com / admin123");
        }
    }
}
