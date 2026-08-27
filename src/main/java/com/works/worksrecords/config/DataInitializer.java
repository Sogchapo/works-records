package com.works.worksrecords.config;

import com.works.worksrecords.model.User;
import com.works.worksrecords.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("superadmin")) {
            User admin = new User();
            admin.setUsername("superadmin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Super Administrator");
            admin.setRoles(new HashSet<>(Set.of(User.Role.ROLE_SUPERADMIN, User.Role.ROLE_USER)));
            
            userRepository.save(admin);
            System.out.println(">>> Superadmin user created automatically.");
        }
    }
}