package com.EticPlus_POC.controller;

import com.EticPlus_POC.dto.UserProfileResponse;
import com.EticPlus_POC.dto.UserUpdateRequest;
import com.EticPlus_POC.exception.BusinessException;
import com.EticPlus_POC.models.Plugin;
import com.EticPlus_POC.models.StoreCategory;
import com.EticPlus_POC.models.User;
import com.EticPlus_POC.service.StoreCategoryService;
import com.EticPlus_POC.service.UserService;
import com.EticPlus_POC.utility.AuthenticationRequest;
import com.EticPlus_POC.utility.AuthenticationResponse;
import com.EticPlus_POC.utility.JwtUtil;
import com.EticPlus_POC.utility.UserRegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StoreCategoryService storeCategoryService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest request) {
        validateRegistrationRequest(request);
        StoreCategory category = storeCategoryService.findByName(request.getCategory());
        if (category == null) {
            throw new BusinessException("INVALID_CATEGORY", "Invalid category");
        }
        User user = new User(request.getStoreName(), category, request.getPassword(), request.getPackageType());
        User savedUser = userService.registerUser(user);
        return ResponseEntity.ok(savedUser);
    }

    private void validateRegistrationRequest(UserRegistrationRequest request) {
        userService.validateStoreName(request.getStoreName());
        userService.validatePassword(request.getPassword());

        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new BusinessException("CATEGORY_EMPTY", "Category cannot be empty");
        }

        if (request.getPackageType() == null) {
            throw new BusinessException("PACKAGE_TYPE_NULL", "Package type cannot be null");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getStoreName());

            if (passwordEncoder.matches(authenticationRequest.getPassword(), userDetails.getPassword())) {
                String jwt = jwtUtil.generateToken(userDetails);
                return ResponseEntity.ok(new AuthenticationResponse(jwt));
            } else {
                throw new BusinessException("INVALID_CREDENTIALS", "Invalid credentials");
            }
        } catch (UsernameNotFoundException e) {
            throw new BusinessException("USER_NOT_FOUND", "User not found");
        } catch (Exception e) {
            throw new BusinessException("LOGIN_ERROR", "An error occurred during login: " + e.getMessage());
        }
    }

    @GetMapping("/home")
    public ResponseEntity<?> getHomePage(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            validateAuthorizationHeader(authorizationHeader);
            String username = getUsernameFromToken(authorizationHeader);

            User user = userService.findByStoreName(username).orElseThrow(() ->
                    new BusinessException("USER_NOT_FOUND", "User not found"));

            List<Plugin> plugins = user.getPlugins();
            return ResponseEntity.ok(plugins);
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving home page");
        }
    }

    @PostMapping("/togglePlugin")
    public ResponseEntity<?> togglePlugin(@RequestHeader("Authorization") String authorizationHeader, @RequestParam String pluginName) {
        try {
            validateAuthorizationHeader(authorizationHeader);
            String username = getUsernameFromToken(authorizationHeader);

            User user = userService.findByStoreName(username).orElseThrow(() ->
                    new BusinessException("USER_NOT_FOUND", "User not found"));

            userService.togglePlugin(user, pluginName);
            return ResponseEntity.ok("Plugin status updated.");
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error toggling plugin");
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<StoreCategory>> getAllCategories() {
        List<StoreCategory> categories = storeCategoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            validateAuthorizationHeader(authorizationHeader);
            String username = getUsernameFromToken(authorizationHeader);

            User user = userService.findByStoreName(username).orElseThrow(() ->
                    new BusinessException("USER_NOT_FOUND", "User not found"));

            UserProfileResponse profileResponse = new UserProfileResponse(user.getStoreName(),user.getPassword(), user.getCategory().getName(), String.valueOf(user.getPackageType()));

            return ResponseEntity.ok(profileResponse);
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving profile.");
        }
    }

    @PutMapping("/updateProfile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authorizationHeader, @RequestBody UserUpdateRequest updateRequest) {
        try {
            validateAuthorizationHeader(authorizationHeader);
            String username = getUsernameFromToken(authorizationHeader);

            User user = userService.findByStoreName(username).orElseThrow(() ->
                    new BusinessException("USER_NOT_FOUND", "User not found"));

            boolean isUpdated = updateUserProfile(user, updateRequest);
            if (isUpdated) {
                User updatedUser = userService.updateUser(user);
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.ok("No updates made.");
            }
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating profile");
        }
    }

    private boolean updateUserProfile(User user, UserUpdateRequest updateRequest) {
        boolean isUpdated = false;

        if (updateRequest.getStoreName() != null && !updateRequest.getStoreName().trim().isEmpty()) {
            userService.validateStoreName(updateRequest.getStoreName());
            user.setStoreName(updateRequest.getStoreName());
            isUpdated = true;
        }

        if (updateRequest.getCategory() != null && !updateRequest.getCategory().trim().isEmpty()) {
            StoreCategory category = storeCategoryService.findByName(updateRequest.getCategory());
            if (category != null) {
                user.setCategory(category);
                isUpdated = true;
            } else {
                throw new BusinessException("INVALID_CATEGORY", "Invalid category.");
            }
        }

        if (updateRequest.getPackageType() != null) {
            user.setPackageType(updateRequest.getPackageType());
            user.initializePlugins();
            isUpdated = true;
        }

        if (updateRequest.getPassword() != null && !updateRequest.getPassword().trim().isEmpty()) {
            if (updateRequest.getConfirmPassword() != null && updateRequest.getPassword().equals(updateRequest.getConfirmPassword())) {
                userService.validatePassword(updateRequest.getPassword());
                user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
                isUpdated = true;
            } else {
                throw new BusinessException("PASSWORD_MISMATCH", "Passwords do not match.");
            }
        }

        return isUpdated;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String jwtToken = authorizationHeader.substring(7);
                jwtUtil.invalidateToken(jwtToken);
            }
            return ResponseEntity.ok("User logged out.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error during logout");
        }
    }

    @PostMapping("/deleteAccount")
    public ResponseEntity<?> deleteAccount(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            validateAuthorizationHeader(authorizationHeader);
            String username = getUsernameFromToken(authorizationHeader);

            User user = userService.findByStoreName(username).orElseThrow(() ->
                    new BusinessException("USER_NOT_FOUND", "User not found"));

            boolean success = userService.deleteAccountById(user.getId());
            if (success) {
                return ResponseEntity.ok("Account has been deleted.");
            } else {
                return ResponseEntity.badRequest().body("Error deleting account.");
            }
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error during account deletion.");
        }
    }

    private void validateAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BusinessException("INVALID_AUTH_HEADER", "Authorization header missing or invalid.");
        }
    }

    private String getUsernameFromToken(String authorizationHeader) {
        String jwtToken = authorizationHeader.substring(7);
        return jwtUtil.extractUsername(jwtToken);
    }
}
