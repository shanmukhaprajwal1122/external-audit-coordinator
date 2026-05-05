package com.internship.tool.controller;

import com.internship.tool.entity.User;
import com.internship.tool.repository.UserRepository;
import com.internship.tool.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Endpoints for user login and JWT generation")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Schema(description = "Login Request DTO")
    @Data
    public static class AuthRequest {
        @Schema(description = "User email", example = "admin@example.com")
        private String email;
        @Schema(description = "User password", example = "Admin@123")
        private String password;
    }

    @Schema(description = "Login Response DTO containing JWT")
    @Data
    public static class AuthResponse {
        @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        private String token;
        @Schema(description = "Token Type", example = "Bearer")
        private String type = "Bearer";
        @Schema(description = "User email", example = "admin@example.com")
        private String email;
        @Schema(description = "User Role", example = "ADMIN")
        private String role;
        
        public AuthResponse(String token, String email, String role) {
            this.token = token;
            this.email = email;
            this.role = role;
        }
    }

    @Operation(summary = "Login and generate JWT", description = "Authenticates a user and returns a JWT token.")
    @ApiResponse(responseCode = "200", description = "Successfully authenticated")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);
        
        User user = userRepository.findByEmail(authRequest.getEmail()).orElseThrow();

return ResponseEntity.ok(new AuthResponse(jwt, user.getEmail(), user.getRole().name()));
    }

    @Operation(summary = "Get current user", description = "Returns the authenticated user's information.")
    @ApiResponse(responseCode = "200", description = "User info retrieved")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(java.util.Map.of(
            "email", user.getEmail(),
            "role", user.getRole().name(),
            "name", user.getFullName()
        ));
    }
}

