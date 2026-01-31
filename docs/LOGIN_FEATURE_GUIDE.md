# Simple Login Feature Implementation Guide

This guide will walk you through creating a basic login feature for your application.

**🛑 IMPORTANT SECURITY WARNING 🛑**

This implementation is for educational purposes only. It compares passwords in plain text, which is **extremely insecure**. In a real-world application, you **MUST** hash passwords using a strong algorithm like Bcrypt before storing them and when verifying them. Storing plain text passwords is a major security vulnerability.

---

## Overview

We will create a simple login endpoint `/api/auth/login` that accepts a `POST` request with a username and password. If the credentials are correct, it will return the user's details.

Here are the steps:
1.  **Create a Request DTO** to handle login data.
2.  **Create a `UserRepository`** to find users in the database.
3.  **Create an `AuthService`** to contain the login logic.
4.  **Create an `AuthController`** to expose the login endpoint.

---

### Step 1: Create Login Request DTO

First, let's create a Data Transfer Object (DTO) to model the incoming login request.

Create the directory `src/main/java/org/luun/kitchencontrolbev1/dto/request/` if it doesn't exist.

Inside that directory, create a new file named `LoginRequest.java`:

**File:** `src/main/java/org/luun/kitchencontrolbev1/dto/request/LoginRequest.java`
```java
package org.luun.kitchencontrolbev1.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String username;
    private String password;
}
```

---

### Step 2: Create UserRepository

Next, we need a repository to interact with the `User` entity in the database. Create a new file in the `repository` package.

**File:** `src/main/java/org/luun/kitchencontrolbev1/repository/UserRepository.java`
```java
package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // Method to find a user by their username
    Optional<User> findByUsername(String username);
}
```
*This interface allows us to perform database operations on the `User` entity, including a custom method to find a user by their username.*

---

### Step 3: Create AuthService

The service layer will handle the business logic for authentication.

First, create the service interface:

**File:** `src/main/java/org/luun/kitchencontrolbev1/service/AuthService.java`
```java
package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.LoginRequest;
import org.luun.kitchencontrolbev1.entity.User;

public interface AuthService {
    User login(LoginRequest loginRequest);
}
```

Now, create the implementation for this service. Create the `impl` directory if it doesn't exist.

**File:** `src/main/java/org/luun/kitchencontrolbev1/service/impl/AuthServiceImpl.java`
```java
package org.luun.kitchencontrolbev1.service.impl;

import org.luun.kitchencontrolbev1.dto.request.LoginRequest;
import org.luun.kitchencontrolbev1.entity.User;
import org.luun.kitchencontrolbev1.repository.UserRepository;
import org.luun.kitchencontrolbev1.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User login(LoginRequest loginRequest) {
        // Find the user by username
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // IMPORTANT: Plain text password comparison (INSECURE!)
        // In a real app, you would use a password encoder to compare hashes.
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }
}
```
*This service finds the user and checks if the provided password matches the one in the database.*

---

### Step 4: Create AuthController

Finally, create a controller to expose the `/login` endpoint.

**File:** `src/main/java/org/luun/kitchencontrolbev1/controller/AuthController.java`
```java
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
@RequestMapping("/api/auth")
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
```
*This controller defines the `POST /api/auth/login` endpoint. It takes the login credentials, passes them to the `AuthService`, and returns the user object on success or a 401 Unauthorized error on failure.*

---

### How to Use

1.  **Run your Spring Boot application.**
2.  **Use a tool like Postman or `curl` to send a `POST` request to `http://localhost:8080/api/auth/login`.**
3.  **Set the `Content-Type` header to `application/json`.**
4.  **Provide a JSON body with the username and password:**
    ```json
    {
      "username": "your_username",
      "password": "your_password"
    }
    ```

If the credentials are valid, you will receive a `200 OK` response with the user's details. Otherwise, you will get a `401 Unauthorized` response.
