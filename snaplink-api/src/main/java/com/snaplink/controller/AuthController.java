package com.snaplink.controller;

import com.snaplink.model.dto.AuthRequest;
import com.snaplink.model.dto.AuthResponse;
import com.snaplink.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

/**
 * REST controller for authentication (register / login).
 *
 * <p>These endpoints are public — no JWT required.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user account.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        authService.register(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse("User registered successfully. You can now log in."));
    }

    /**
     * Authenticates a user and returns JWT tokens.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthenticationResultType result = authService.login(request.getEmail(), request.getPassword());

        AuthResponse response = new AuthResponse(
                result.idToken(),
                result.accessToken(),
                result.refreshToken(),
                (long) result.expiresIn()
        );
        return ResponseEntity.ok(response);
    }
}
