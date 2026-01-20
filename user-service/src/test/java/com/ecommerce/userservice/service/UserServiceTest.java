package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.*;
import com.ecommerce.userservice.exception.ResourceNotFoundException;
import com.ecommerce.userservice.exception.UserAlreadyExistsException;
import com.ecommerce.userservice.model.User;
import com.ecommerce.userservice.model.UserRole;
import com.ecommerce.userservice.repository.UserRepository;
import com.ecommerce.userservice.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 * Tests the core business logic of user management including registration, authentication, and profile management
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user123");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhone("1234567890");
        testUser.setRole(UserRole.CLIENT);

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setPhone("1234567890");
        registerRequest.setRole(UserRole.CLIENT);
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegisterUser_Success() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(testUser.getEmail())).thenReturn("jwt-token");

        // Act
        AuthResponse response = userService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertNotNull(response.getUser());
        assertEquals("test@example.com", response.getUser().getEmail());
        assertEquals("John", response.getUser().getFirstName());
        
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when registering user with existing email")
    void testRegisterUser_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.register(registerRequest);
        });

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLogin_Success() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));

        // Act
        AuthResponse response = userService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("test@example.com", response.getUser().getEmail());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
    }

    @Test
    @DisplayName("Should retrieve user profile by email")
    void testGetProfile_Success() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        UserResponse userResponse = userService.getProfile("test@example.com");

        // Assert
        assertNotNull(userResponse);
        assertEquals("test@example.com", userResponse.getEmail());
        assertEquals("John", userResponse.getFirstName());
        assertEquals("Doe", userResponse.getLastName());
        
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should throw exception when user profile not found")
    void testGetProfile_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getProfile("nonexistent@example.com");
        });

        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should successfully update user profile")
    void testUpdateProfile_Success() {
        // Arrange
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setPhone("9876543210");

        User updatedUser = new User();
        updatedUser.setId("user123");
        updatedUser.setEmail("test@example.com");
        updatedUser.setFirstName("Jane");
        updatedUser.setLastName("Smith");
        updatedUser.setPhone("9876543210");
        updatedUser.setRole(UserRole.CLIENT);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        UserResponse response = userService.updateProfile("test@example.com", updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertEquals("9876543210", response.getPhone());
        
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should retrieve user by ID")
    void testGetUserById_Success() {
        // Arrange
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

        // Act
        UserResponse response = userService.getUserById("user123");

        // Assert
        assertNotNull(response);
        assertEquals("user123", response.getId());
        assertEquals("test@example.com", response.getEmail());
        
        verify(userRepository).findById("user123");
    }
}
