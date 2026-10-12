package com.pestdetection.controller;

import com.pestdetection.entity.User;
import com.pestdetection.service.UserService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @GetMapping
    public List<User> list() {
        return users.list();
    }

    @GetMapping("/me")
    public User me(Authentication auth) {
        return users.getByUsername(auth.getName());
    }

    @GetMapping("/{id}")
    public User get(@PathVariable Long id) {
        return users.get(id);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User body) {
        return users.update(id, body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        users.delete(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
