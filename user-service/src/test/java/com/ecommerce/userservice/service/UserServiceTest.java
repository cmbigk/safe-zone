package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.*;
import com.ecommerce.userservice.exception.ResourceNotFoundException;
import com.ecommerce.userservice.exception.UnauthorizedException;
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
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    
    // Test constants
    private static final String TEST_USER_ID = "user123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String TEST_PHONE = "1234567890";
    private static final String JWT_TOKEN = "jwt-token";
    private static final String NONEXISTENT_EMAIL = "nonexistent@example.com";
    private static final String DIFFERENT_USER_ID = "user456";
    private static final String DIFFERENT_EMAIL = "different@example.com";
    private static final String UPDATED_FIRST_NAME = "Jane";
    private static final String UPDATED_LAST_NAME = "Smith";
    private static final String UPDATED_PHONE = "9876543210";
    private static final String TEST_AVATAR_URL = "/avatars/avatar.jpg";
    private static final String UPDATED_AVATAR_URL = "/uploads/avatars/new-avatar.jpg";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword(ENCODED_PASSWORD);
        testUser.setFirstName(TEST_FIRST_NAME);
        testUser.setLastName(TEST_LAST_NAME);
        testUser.setPhone(TEST_PHONE);
        testUser.setRole(UserRole.CLIENT);

        registerRequest = new RegisterRequest();
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setFirstName(TEST_FIRST_NAME);
        registerRequest.setLastName(TEST_LAST_NAME);
        registerRequest.setPhone(TEST_PHONE);
        registerRequest.setRole(UserRole.CLIENT);
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegisterUser_Success() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(testUser.getEmail())).thenReturn(JWT_TOKEN);

        // Act
        AuthResponse response = userService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals(JWT_TOKEN, response.getToken());
        assertNotNull(response.getUser());
        assertEquals(TEST_EMAIL, response.getUser().getEmail());
        assertEquals(TEST_FIRST_NAME, response.getUser().getFirstName());
        
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
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn(JWT_TOKEN);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));

        // Act
        AuthResponse response = userService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(JWT_TOKEN, response.getToken());
        assertEquals(TEST_EMAIL, response.getUser().getEmail());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
    }

    @Test
    @DisplayName("Should retrieve user profile by email")
    void testGetProfile_Success() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // Act
        UserResponse userResponse = userService.getProfile(TEST_EMAIL);

        // Assert
        assertNotNull(userResponse);
        assertEquals(TEST_EMAIL, userResponse.getEmail());
        assertEquals(TEST_FIRST_NAME, userResponse.getFirstName());
        assertEquals(TEST_LAST_NAME, userResponse.getLastName());
        
        verify(userRepository).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should throw exception when user profile not found")
    void testGetProfile_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getProfile(NONEXISTENT_EMAIL);
        });

        verify(userRepository).findByEmail(NONEXISTENT_EMAIL);
    }

    @Test
    @DisplayName("Should successfully update user profile")
    void testUpdateProfile_Success() {
        // Arrange
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setFirstName(UPDATED_FIRST_NAME);
        updateRequest.setLastName(UPDATED_LAST_NAME);
        updateRequest.setPhone(UPDATED_PHONE);

        User updatedUser = new User();
        updatedUser.setId(TEST_USER_ID);
        updatedUser.setEmail(TEST_EMAIL);
        updatedUser.setFirstName(UPDATED_FIRST_NAME);
        updatedUser.setLastName(UPDATED_LAST_NAME);
        updatedUser.setPhone(UPDATED_PHONE);
        updatedUser.setRole(UserRole.CLIENT);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        UserResponse response = userService.updateProfile(TEST_EMAIL, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(UPDATED_FIRST_NAME, response.getFirstName());
        assertEquals(UPDATED_LAST_NAME, response.getLastName());
        assertEquals(UPDATED_PHONE, response.getPhone());
        
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should retrieve user by ID")
    void testGetUserById_Success() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        UserResponse response = userService.getUserById(TEST_USER_ID);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_USER_ID, response.getId());
        assertEquals(TEST_EMAIL, response.getEmail());
        
        verify(userRepository).findById(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should throw exception when getting user by non-existent ID")
    void testGetUserById_NotFound() {
        // Arrange
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            userService.getUserById(NONEXISTENT_EMAIL)
        );

        verify(userRepository).findById(NONEXISTENT_EMAIL);
    }

    @Test
    @DisplayName("Should throw exception when updating profile for non-existent user")
    void testUpdateProfile_UserNotFound() {
        // Arrange
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setFirstName(UPDATED_FIRST_NAME);
        updateRequest.setLastName(UPDATED_LAST_NAME);
        updateRequest.setPhone(UPDATED_PHONE);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            userService.updateProfile(NONEXISTENT_EMAIL, updateRequest)
        );

        verify(userRepository).findByEmail(NONEXISTENT_EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully update profile by ID")
    void testUpdateProfileById_Success() {
        // Arrange
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setFirstName(UPDATED_FIRST_NAME);
        updateRequest.setLastName(UPDATED_LAST_NAME);
        updateRequest.setPhone(UPDATED_PHONE);
        updateRequest.setAvatarUrl(UPDATED_AVATAR_URL);

        User updatedUser = new User();
        updatedUser.setId(TEST_USER_ID);
        updatedUser.setEmail(TEST_EMAIL);
        updatedUser.setFirstName(UPDATED_FIRST_NAME);
        updatedUser.setLastName(UPDATED_LAST_NAME);
        updatedUser.setPhone(UPDATED_PHONE);
        updatedUser.setAvatarUrl(UPDATED_AVATAR_URL);
        updatedUser.setRole(UserRole.CLIENT);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        UserResponse response = userService.updateProfileById(TEST_USER_ID, TEST_EMAIL, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(UPDATED_FIRST_NAME, response.getFirstName());
        assertEquals(UPDATED_LAST_NAME, response.getLastName());
        assertEquals(UPDATED_PHONE, response.getPhone());
        assertEquals(UPDATED_AVATAR_URL, response.getAvatarUrl());
        
        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating profile by ID for different user")
    void testUpdateProfileById_Unauthorized() {
        // Arrange
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setFirstName(UPDATED_FIRST_NAME);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
            userService.updateProfileById(TEST_USER_ID, DIFFERENT_EMAIL, updateRequest)
        );

        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating profile by non-existent ID")
    void testUpdateProfileById_NotFound() {
        // Arrange
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setFirstName(UPDATED_FIRST_NAME);

        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            userService.updateProfileById(DIFFERENT_USER_ID, TEST_EMAIL, updateRequest)
        );

        verify(userRepository).findById(DIFFERENT_USER_ID);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully upload avatar")
    void testUploadAvatar_Success() throws IOException {
        // Arrange
        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                "image/jpeg",
                "avatar-content".getBytes()
        );

        User updatedUser = new User();
        updatedUser.setId(TEST_USER_ID);
        updatedUser.setEmail(TEST_EMAIL);
        updatedUser.setAvatarUrl(TEST_AVATAR_URL);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        UserResponse response = userService.uploadAvatar(TEST_EMAIL, avatarFile);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getAvatarUrl());
        
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject avatar file exceeding size limit")
    void testUploadAvatar_FileTooLarge() {
        // Arrange
        byte[] largeContent = new byte[3 * 1024 * 1024]; // 3MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "avatar",
                "large-avatar.jpg",
                "image/jpeg",
                largeContent
        );

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            userService.uploadAvatar(TEST_EMAIL, largeFile)
        );

        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject non-image avatar file")
    void testUploadAvatar_InvalidFileType() {
        // Arrange
        MockMultipartFile pdfFile = new MockMultipartFile(
                "avatar",
                "document.pdf",
                "application/pdf",
                "pdf-content".getBytes()
        );

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            userService.uploadAvatar(TEST_EMAIL, pdfFile)
        );

        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when uploading avatar for non-existent user")
    void testUploadAvatar_UserNotFound() {
        // Arrange
        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                "image/jpeg",
                "avatar-content".getBytes()
        );

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            userService.uploadAvatar(NONEXISTENT_EMAIL, avatarFile)
        );

        verify(userRepository).findByEmail(NONEXISTENT_EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully upload avatar by ID")
    void testUploadAvatarById_Success() throws IOException {
        // Arrange
        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                "image/jpeg",
                "avatar-content".getBytes()
        );

        User updatedUser = new User();
        updatedUser.setId(TEST_USER_ID);
        updatedUser.setEmail(TEST_EMAIL);
        updatedUser.setAvatarUrl(TEST_AVATAR_URL);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        UserResponse response = userService.uploadAvatarById(TEST_USER_ID, TEST_EMAIL, avatarFile);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getAvatarUrl());
        
        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when uploading avatar by ID for different user")
    void testUploadAvatarById_Unauthorized() {
        // Arrange
        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                "image/jpeg",
                "avatar-content".getBytes()
        );

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
            userService.uploadAvatarById(TEST_USER_ID, DIFFERENT_EMAIL, avatarFile)
        );

        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject avatar by ID with invalid file type")
    void testUploadAvatarById_InvalidFileType() {
        // Arrange
        MockMultipartFile pdfFile = new MockMultipartFile(
                "avatar",
                "document.pdf",
                "application/pdf",
                "pdf-content".getBytes()
        );

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            userService.uploadAvatarById(TEST_USER_ID, TEST_EMAIL, pdfFile)
        );

        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should register seller successfully")
    void testRegisterSeller_Success() {
        // Arrange
        RegisterRequest sellerRequest = new RegisterRequest();
        sellerRequest.setEmail("seller@example.com");
        sellerRequest.setPassword(TEST_PASSWORD);
        sellerRequest.setFirstName(TEST_FIRST_NAME);
        sellerRequest.setLastName(TEST_LAST_NAME);
        sellerRequest.setPhone(TEST_PHONE);
        sellerRequest.setRole(UserRole.SELLER);

        User sellerUser = new User();
        sellerUser.setId("seller123");
        sellerUser.setEmail("seller@example.com");
        sellerUser.setRole(UserRole.SELLER);

        when(userRepository.existsByEmail(sellerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(sellerRequest.getPassword())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(sellerUser);
        when(jwtTokenProvider.generateToken(sellerUser.getEmail())).thenReturn(JWT_TOKEN);

        // Act
        AuthResponse response = userService.register(sellerRequest);

        // Assert
        assertNotNull(response);
        assertEquals(JWT_TOKEN, response.getToken());
        assertEquals(UserRole.SELLER, response.getUser().getRole());
        
        verify(userRepository).existsByEmail(sellerRequest.getEmail());
        verify(userRepository).save(any(User.class));
    }
}
