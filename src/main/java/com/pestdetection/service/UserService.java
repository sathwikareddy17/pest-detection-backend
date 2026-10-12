package com.pestdetection.service;

import com.pestdetection.entity.User;
import com.pestdetection.exception.ResourceNotFoundException;
import com.pestdetection.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<User> list() {
        return users.findAll();
    }

    @Transactional(readOnly = true)
    public User get(Long id) {
        return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + username + " was not found"));
    }

    /** Updates username, email and role, and the password only when a new one is supplied. */
    @Transactional
    public User update(Long id, User in) {
        User user = get(id);

        if (in.getUsername() != null && !in.getUsername().isBlank()) {
            String name = in.getUsername().trim();
            if (!name.equals(user.getUsername()) && users.existsByUsername(name)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username " + name + " is taken");
            }
            user.setUsername(name);
        }
        if (in.getEmail() != null) {
            user.setEmail(in.getEmail().trim());
        }
        if (in.getRole() != null) {
            String role = in.getRole().trim().toUpperCase();
            if (!role.equals("USER") && !role.equals("ADMIN")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be USER or ADMIN");
            }
            user.setRole(role);
        }
        if (in.getPassword() != null && !in.getPassword().isBlank()) {
            if (in.getPassword().length() < 6) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
            }
            user.setPassword(encoder.encode(in.getPassword()));
        }
        return users.save(user);
    }

    @Transactional
    public void delete(Long id, String currentUsername) {
        User user = get(id);
        if (user.getUsername().equals(currentUsername)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        }
        users.delete(user);
    }
}
