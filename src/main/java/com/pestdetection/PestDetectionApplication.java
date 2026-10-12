package com.pestdetection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// Users live in our own table and authenticate with JWT, so the default in-memory user is switched off.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class PestDetectionApplication {
    public static void main(String[] args) {
        SpringApplication.run(PestDetectionApplication.class, args);
    }
}
