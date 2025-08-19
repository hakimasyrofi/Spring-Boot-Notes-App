package com.spring.notes.app.config;

import com.spring.notes.app.entity.User;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "mySecretKeyForTestingPurposesOnlyAndShouldBeLongEnoughForJWTRequirements");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // 24 hours

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testGenerateToken_Success() {
        // When
        String token = jwtUtil.generateToken(testUser);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Verify token structure (should have 3 parts separated by dots)
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    void testGenerateToken_ContainsUserInfo() {
        // When
        String token = jwtUtil.generateToken(testUser);

        // Then
    SecretKey key = (SecretKey) ReflectionTestUtils.invokeMethod(jwtUtil, "getSignInKey");
    Claims claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();

        assertEquals(testUser.getUsername(), claims.getSubject());
    }

    @Test
    void testValidateToken_ValidToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
    boolean isValid = jwtUtil.isTokenValid(token, testUser);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
    boolean isValid = jwtUtil.isTokenValid(invalidToken, testUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_EmptyToken() {
        // Given
        String emptyToken = "";

        // When
    boolean isValid = jwtUtil.isTokenValid(emptyToken, testUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_NullToken() {
        // Given
        String nullToken = null;

        // When
    boolean isValid = jwtUtil.isTokenValid(nullToken, testUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testExtractUsername_ValidToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        String username = jwtUtil.extractUsername(token);

        // Then
        assertEquals(testUser.getUsername(), username);
    }

    @Test
    void testExtractUsername_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtUtil.extractUsername(invalidToken);
        });
    }

    @Test
    void testExtractExpiration_ValidToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        Date expiration = jwtUtil.extractExpiration(token);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date())); // Should be in the future
    }

    @Test
    void testIsTokenExpired_ValidToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
    // Use reflection to access private method
    boolean isExpired = (boolean) ReflectionTestUtils.invokeMethod(jwtUtil, "isTokenExpired", token);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void testGenerateToken_DifferentUsers() {
        // Given
        User user1 = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .role(User.Role.USER)
                .build();

        User user2 = User.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .role(User.Role.ADMIN)
                .build();

        // When
        String token1 = jwtUtil.generateToken(user1);
        String token2 = jwtUtil.generateToken(user2);

        // Then
        assertNotEquals(token1, token2);
        assertEquals("user1", jwtUtil.extractUsername(token1));
        assertEquals("user2", jwtUtil.extractUsername(token2));
    }

    @Test
    void testTokenExpiration() {
        // Given - Set a very short expiration time (1 second)
        JwtUtil shortExpirationJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "secret", "mySecretKeyForTestingPurposesOnlyAndShouldBeLongEnoughForJWTRequirements");
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "expiration", 1000L); // 1 second

        String token = shortExpirationJwtUtil.generateToken(testUser);

        // When - Token should be valid initially
    boolean isValidInitially = shortExpirationJwtUtil.isTokenValid(token, testUser);

        // Then
        assertTrue(isValidInitially);

        // Note: Testing actual expiration would require waiting, which is not practical in unit tests
        // In a real scenario, you might use a test configuration with very short expiration times
        // and use Thread.sleep() in integration tests
    }
}
