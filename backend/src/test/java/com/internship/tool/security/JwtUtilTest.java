package com.internship.tool.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    // Use a fixed 256-bit base64-encoded string for the HMAC-SHA secret
    private final String testSecret = "NmZlN2ZhM2YyYTM1NDRiNmE4ZTY1YThlNThlM2U3NjZiNDdhOGI3ZjEyMzQ1Njc4OTA=";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Inject values using Spring's ReflectionTestUtils
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 3600000); // 1 hour

        userDetails = new User("testuser@example.com", "password", new ArrayList<>());
    }

    @Test
    void generateToken_AndExtractUsername_Success() {
        String token = jwtUtil.generateToken(userDetails);

        assertNotNull(token);
        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals("testuser@example.com", extractedUsername);
    }

    @Test
    void validateToken_ReturnsTrue_ForValidToken() {
        String token = jwtUtil.generateToken(userDetails);

        Boolean isValid = jwtUtil.validateToken(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    void validateToken_ReturnsFalse_ForDifferentUser() {
        String token = jwtUtil.generateToken(userDetails);
        UserDetails differentUser = new User("other@example.com", "pass", new ArrayList<>());

        Boolean isValid = jwtUtil.validateToken(token, differentUser);
        assertFalse(isValid);
    }
}
