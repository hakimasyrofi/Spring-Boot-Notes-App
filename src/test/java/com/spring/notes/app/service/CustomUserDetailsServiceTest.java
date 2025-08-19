package com.spring.notes.app.service;

import com.spring.notes.app.entity.User;
import com.spring.notes.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testLoadUserByUsername_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        
        // Check authorities
        assertFalse(userDetails.getAuthorities().isEmpty());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistent");
        });

        assertEquals("User not found with username: nonexistent", exception.getMessage());
    }

    @Test
    void testLoadUserByUsername_DisabledUser() {
        // Given
        User disabledUser = User.builder()
                .id(2L)
                .username("disableduser")
                .email("disabled@example.com")
                .password("encodedPassword")
                .firstName("Disabled")
                .lastName("User")
                .role(User.Role.USER)
                .enabled(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("disableduser")).thenReturn(Optional.of(disabledUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("disableduser");

        // Then
        assertNotNull(userDetails);
        assertEquals("disableduser", userDetails.getUsername());
        assertFalse(userDetails.isEnabled());
    }

    @Test
    void testLoadUserByUsername_AdminUser() {
        // Given
        User adminUser = User.builder()
                .id(3L)
                .username("adminuser")
                .email("admin@example.com")
                .password("encodedPassword")
                .firstName("Admin")
                .lastName("User")
                .role(User.Role.ADMIN)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("adminuser")).thenReturn(Optional.of(adminUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("adminuser");

        // Then
        assertNotNull(userDetails);
        assertEquals("adminuser", userDetails.getUsername());
        assertTrue(userDetails.isEnabled());
        
        // Check authorities for admin
        assertFalse(userDetails.getAuthorities().isEmpty());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testLoadUserByUsername_EmptyUsername() {
        // Given
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("");
        });

        assertEquals("User not found with username: ", exception.getMessage());
    }

    @Test
    void testLoadUserByUsername_NullUsername() {
        // Given
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(null);
        });

        assertEquals("User not found with username: null", exception.getMessage());
    }

    @Test
    void testLoadUserByUsername_UserWithNullFields() {
        // Given
        User userWithNullFields = User.builder()
                .id(4L)
                .username("nulluser")
                .email(null)
                .password("encodedPassword")
                .firstName(null)
                .lastName(null)
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("nulluser")).thenReturn(Optional.of(userWithNullFields));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("nulluser");

        // Then
        assertNotNull(userDetails);
        assertEquals("nulluser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        
        // Should still have authorities even with null fields
        assertFalse(userDetails.getAuthorities().isEmpty());
    }
}
