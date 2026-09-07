package com.civicAI.backend.config;

import com.civicAI.backend.entity.User;
import com.civicAI.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class OfficerSeeder {

    private static final Logger log = LoggerFactory.getLogger(OfficerSeeder.class);

    @Bean
    public CommandLineRunner seedOfficer(UserRepository userRepository,
                                         PasswordEncoder passwordEncoder) {
        return args -> {
            String email = "officer@civicai.test";
            if (!userRepository.existsByEmail(email)) {
                User officer = new User();
                officer.setName("Demo Officer");
                officer.setEmail(email);
                officer.setPasswordHash(passwordEncoder.encode("officer123"));
                officer.setRole("OFFICER");
                userRepository.save(officer);
                log.info("Seeded demo OFFICER account {}", email);
            }
        };
    }
}
