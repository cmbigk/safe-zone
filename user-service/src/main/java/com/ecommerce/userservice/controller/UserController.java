package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.dto.UpdateProfileRequest;
import com.ecommerce.userservice.dto.UserResponse;
import com.ecommerce.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUserById(
            @PathVariable String id,
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        String authenticatedEmail = authentication.getName();
        UserResponse response = userService.updateProfileById(id, authenticatedEmail, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/avatar")
    public ResponseEntity<UserResponse> uploadAvatarById(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {
        String authenticatedEmail = authentication.getName();
        UserResponse response = userService.uploadAvatarById(id, authenticatedEmail, file);
        return ResponseEntity.ok(response);
    }
}
