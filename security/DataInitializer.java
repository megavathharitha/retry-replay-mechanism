package com.example.transactionretryreplay.security;


import com.example.transactionretryreplay.model.Role;
import com.example.transactionretryreplay.model.User;
import com.example.transactionretryreplay.repository.RoleRepository;
import com.example.transactionretryreplay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepository.save(adminRole);
        }

        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            roleRepository.save(userRole);
        }

        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEnabled(true);
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
            adminUser.getRoles().add(adminRole);
            userRepository.save(adminUser);
        }

        if (userRepository.findByUsername("user").isEmpty()) {
            User regularUser = new User();
            regularUser.setUsername("user");
            regularUser.setPassword(passwordEncoder.encode("user456"));
            regularUser.setEnabled(true);
            Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow();
            regularUser.getRoles().add(userRole);
            userRepository.save(regularUser);
        }
    }
}