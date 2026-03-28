
# Secure Login with JWT and Spring Security

This guide will walk you through implementing a secure authentication and authorization system in your Spring Boot application using JSON Web Tokens (JWT) and Spring Security.

This guide assumes you have a basic login system in place. We will build upon it to add robust security.

**Core Concepts:**

*   **Spring Security:** A powerful framework that provides authentication and authorization to Java applications.
*   **JWT (JSON Web Token):** A compact, URL-safe means of representing claims to be transferred between two parties. We will use it to handle user authentication for subsequent API requests after the initial login.
*   **Bcrypt:** A strong password-hashing algorithm to securely store user passwords.

---

## **Step 0: Update `pom.xml` Dependencies**

First, you need to add Spring Security for authentication and JWT libraries for token management. Also, the Spring Boot version in your `pom.xml` (`4.0.1`) is not valid; we will correct it to a recent stable version like `3.2.2`.

1.  **Correct Parent Version:**
    Replace `<version>4.0.1</version>` in the `<parent>` section with `<version>3.2.2</version>`.

2.  **Add Dependencies:**
    Add the following dependencies inside your `<dependencies>` section in `pom.xml`:

    ```xml
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- JWT Libraries -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    ```

After saving `pom.xml`, let your IDE (like IntelliJ or VSCode) sync the new dependencies.

---

## **Step 1: Configure Spring Security**

Create a `config` package inside `...kitchencontrolbev1` for our security configuration classes.

**File:** `src/main/java/org/luun/kitchencontrolbev1/config/SecurityConfig.java`
```java
package org.luun.kitchencontrolbev1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

---

## **Step 2: Update User Entity and Create `CustomUserDetailsService`**

Spring Security needs a way to load user data. We will modify the `User` entity to implement `UserDetails` and create a service to load it.

1.  **Modify `User.java`**
    Update your `User.java` entity to implement the `UserDetails` interface.

    Add `implements UserDetails` to your class definition and implement the required methods. You will also need to adjust the `Role` relationship to work with Spring Security's authorities.

    **Note:** For simplicity, we assume the `Role` entity has a `getRoleName()` method that returns the role as a String (e.g., "ADMIN", "USER").

    **(See `docs/ENTITY_CREATION_GUIDE.md` for guidance on creating the `Role` entity if needed).**

    *The `UserDetails` methods provide Spring Security with user details like authorities (roles), and account status.*

2.  **Create `CustomUserDetailsService.java`**
    This service will load the user from the database.

    **File:** `src/main/java/org/luun/kitchencontrolbev1/service/impl/CustomUserDetailsService.java`
    ```java
    package org.luun.kitchencontrolbev1.service.impl;

    import org.luun.kitchencontrolbev1.repository.UserRepository;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.security.core.userdetails.UserDetailsService;
    import org.springframework.security.core.userdetails.UsernameNotFoundException;
    import org.springframework.stereotype.Service;

    @Service
    public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

        public CustomUserDetailsService(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        }
    }
    ```

---

## **Step 3: Create JWT Service**

This service will handle all JWT operations.

1.  **Create `JwtService.java` Interface**

    **File:** `src/main/java/org/luun/kitchencontrolbev1/service/JwtService.java`
    ```java
    package org.luun.kitchencontrolbev1.service;

    import org.springframework.security.core.userdetails.UserDetails;

    import java.util.Map;

    public interface JwtService {
        String extractUsername(String token);
        String generateToken(UserDetails userDetails);
        String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);
        boolean isTokenValid(String token, UserDetails userDetails);
    }
    ```

2.  **Create `JwtServiceImpl.java` Implementation**

    **File:** `src/main/java/org/luun/kitchencontrolbev1/service/impl/JwtServiceImpl.java`
    ```java
    package org.luun.kitchencontrolbev1.service.impl;

    import io.jsonwebtoken.Claims;
    import io.jsonwebtoken.Jwts;
    import io.jsonwebtoken.SignatureAlgorithm;
    import io.jsonwebtoken.io.Decoders;
    import io.jsonwebtoken.security.Keys;
    import org.luun.kitchencontrolbev1.service.JwtService;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.stereotype.Service;

    import java.security.Key;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.Map;
    import java.util.function.Function;

    @Service
    public class JwtServiceImpl implements JwtService {

        @Value("${jwt.secret}")
        private String jwtSecret;

        @Value("${jwt.expiration}")
        private long jwtExpiration;

        @Override
        public String extractUsername(String token) {
            return extractClaim(token, Claims::getSubject);
        }

        @Override
        public String generateToken(UserDetails userDetails) {
            return generateToken(new HashMap<>(), userDetails);
        }

        @Override
        public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
            return Jwts.builder()
                    .setClaims(extraClaims)
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();
        }

        @Override
        public boolean isTokenValid(String token, UserDetails userDetails) {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        }

        private boolean isTokenExpired(String token) {
            return extractExpiration(token).before(new Date());
        }

        private Date extractExpiration(String token) {
            return extractClaim(token, Claims::getExpiration);
        }

        public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        }

        private Claims extractAllClaims(String token) {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }

        private Key getSignInKey() {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            return Keys.hmacShaKeyFor(keyBytes);
        }
    }
    ```

---

## **Step 4: Create JWT Authentication Filter**

This filter will run once for every request to validate the token.

**File:** `src/main/java/org/luun/kitchencontrolbev1/config/JwtAuthFilter.java`
```java
package org.luun.kitchencontrolbev1.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.service.JwtService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        username = jwtService.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

---

## **Step 5: Update `application.properties`**

Add the JWT secret key and expiration time.

**IMPORTANT:** Use a strong, long, and randomly generated string for your secret key in a production environment.

```properties
# JWT Settings
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250655368566D5970
jwt.expiration=86400000 # 24 hours in milliseconds
```

---

## **Step 6: Update `AuthService` and `AuthController`**

Now, we update the login logic to use Spring Security and return a JWT.

1.  **Create `LoginResponse.java` DTO**
    Create a new DTO to hold the JWT token.

    **File:** `src/main/java/org/luun/kitchencontrolbev1/dto/response/LoginResponse.java`
    ```java
    package org.luun.kitchencontrolbev1.dto.response;

    import lombok.Builder;
    import lombok.Data;

    @Data
    @Builder
    public class LoginResponse {
        private String token;
    }
    ```

2.  **Update `AuthServiceImpl.java`**
    Modify the `login` method to perform proper authentication and generate a token.

    ```java
    // In AuthServiceImpl.java
    
    // Inject dependencies via constructor
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    // New Login method
    public LoginResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        var user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        var jwtToken = jwtService.generateToken(user);
        return LoginResponse.builder().token(jwtToken).build();
    }
    ```
    **Note:** You will need to update the `AuthService` interface to return `LoginResponse` instead of `User`.

3.  **Update `AuthController.java`**
    Update the `login` endpoint to return the `LoginResponse`.

    ```java
    // In AuthController.java
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }
    ```

---

## **Step 7: (Optional but Recommended) User Registration with Password Hashing**

When creating new users, you **MUST** hash their passwords. Here's a simple example of how you could do it in your `UserService`.

```java
// In your user creation/registration service method

@Autowired
private PasswordEncoder passwordEncoder;

public User createUser(UserRequest userRequest) {
    User newUser = new User();
    newUser.setUsername(userRequest.getUsername());
    // HASH the password before saving
    newUser.setPassword(passwordEncoder.encode(userRequest.getPassword())); 
    // ... set other fields
    return userRepository.save(newUser);
}
```
This ensures you never store plain text passwords in your database.
