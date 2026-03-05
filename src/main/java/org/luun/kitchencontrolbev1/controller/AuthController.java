package org.luun.kitchencontrolbev1.controller;

import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.AuthenticationRequest;
import org.luun.kitchencontrolbev1.dto.request.IntrospectRequest;
import org.luun.kitchencontrolbev1.dto.request.LoginRequest;
import org.luun.kitchencontrolbev1.dto.response.AuthenticationResponse;
import org.luun.kitchencontrolbev1.dto.response.IntrospectResponse;
import org.luun.kitchencontrolbev1.entity.User;
import org.luun.kitchencontrolbev1.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for User Authentication and Authorization")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticates a user with username and password and returns user details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", 
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Invalid username or password"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = authService.login(loginRequest);
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/v2/login")
    public AuthenticationResponse loginV2(@RequestBody AuthenticationRequest request) {
        var result = authService.loginV2(request);

        return result;
    }

    @PostMapping("/introspect")
    public IntrospectResponse loginV2(@RequestBody IntrospectRequest request) throws JOSEException, ParseException {
        return authService.introspect(request);
    }
}
