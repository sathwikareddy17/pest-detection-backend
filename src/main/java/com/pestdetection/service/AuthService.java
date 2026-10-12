package com.pestdetection.service;

import com.pestdetection.dto.AuthResponse;
import com.pestdetection.dto.LoginRequest;
import com.pestdetection.dto.RegisterRequest;
import com.pestdetection.entity.User;
import com.pestdetection.repository.UserRepository;
import com.pestdetection.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        // Same message for unknown user and wrong password, so the API does not reveal which usernames exist.
        User user = users.findByUsername(req.username().trim())
                .filter(u -> encoder.matches(req.password(), u.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("Wrong username or password."));
        return new AuthResponse(jwt.generateToken(user), user.getUsername(), user.getRole());
    }

    /** Self-registration always creates a regular USER. Admins promote accounts from the Users page. */
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String username = req.username().trim();
        if (users.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username " + username + " is taken");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(req.email() == null ? null : req.email().trim());
        user.setPassword(encoder.encode(req.password()));
        user.setRole("USER");
        User saved = users.save(user);
        return new AuthResponse(jwt.generateToken(saved), saved.getUsername(), saved.getRole());
    }
}
