package org.luun.kitchencontrolbev1.controller;

import org.luun.kitchencontrolbev1.dto.request.LoginRequest;
import org.luun.kitchencontrolbev1.entity.User;
import org.luun.kitchencontrolbev1.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = authService.login(loginRequest);
            // On successful login, return the user details (excluding password)
            user.setPassword(null); // Avoid sending password back to the client
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            // If login fails, return an unauthorized status
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
