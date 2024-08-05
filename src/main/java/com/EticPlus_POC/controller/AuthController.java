package com.EticPlus_POC.controller;

import com.EticPlus_POC.dto.UserProfileResponse;
import com.EticPlus_POC.dto.UserUpdateRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        try {
            userService.validateStoreName(request.getStoreName());
            userService.validatePassword(request.getPassword());

            if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Category cannot be empty");
            }

            if (request.getPackageType() == null) {
                return ResponseEntity.badRequest().body("Package type cannot be null");
            }

            StoreCategory category = storeCategoryService.findByName(request.getCategory());
            if (category == null) {
                return ResponseEntity.badRequest().body("Invalid category");
            }

            User user = new User(request.getStoreName(), category, request.getPassword(), request.getPackageType());
            User savedUser = userService.registerUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
        try {
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getStoreName());

            if (passwordEncoder.matches(authenticationRequest.getPassword(), userDetails.getPassword())) {
                final String jwt = jwtUtil.generateToken(userDetails);
                return ResponseEntity.ok(new AuthenticationResponse(jwt));
            } else {
                return ResponseEntity.badRequest().body("Invalid credentials");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Authentication failed");
        }
    }

    @GetMapping("/home")
    public ResponseEntity<?> getHomePage(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String jwtToken = authorizationHeader.substring(7);
                String username = jwtUtil.extractUsername(jwtToken);

                User user = userService.findByStoreName(username).orElse(null);

                if (user != null) {
                    List<Plugin> plugins = user.getPlugins();
                    return ResponseEntity.ok(plugins);
                } else {
                    return ResponseEntity.badRequest().body("User not found.");
                }
            } else {
                return ResponseEntity.badRequest().body("Authorization header missing or invalid.");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving home page");
        }
    }

    @PostMapping("/togglePlugin")
    public ResponseEntity<?> togglePlugin(@RequestParam String userId, @RequestParam String pluginName) {
        try {
            User user = userService.findById(userId);
            if (user != null) {
                userService.togglePlugin(user, pluginName);
                return ResponseEntity.ok("Plugin status updated.");
            } else {
                return ResponseEntity.badRequest().body("User not found.");
            }
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
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String jwtToken = authorizationHeader.substring(7);
                String username = jwtUtil.extractUsername(jwtToken);

                User user = userService.findByStoreName(username).orElse(null);

                if (user != null) {
                    UserProfileResponse profileResponse = new UserProfileResponse();
                    profileResponse.setStoreName(user.getStoreName());
                    profileResponse.setPassword(user.getPassword());
                    profileResponse.setCategory(user.getCategory().getName());
                    profileResponse.setPackageType(String.valueOf(user.getPackageType()));

                    return ResponseEntity.ok(profileResponse);
                } else {
                    return ResponseEntity.badRequest().body("User not found.");
                }
            } else {
                return ResponseEntity.badRequest().body("Authorization header missing or invalid.");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving profile.");
        }
    }



    @PutMapping("/updateProfile")
    public ResponseEntity<?> updateProfile(@RequestBody UserUpdateRequest updateRequest) {
        try {
            User user = userService.findById(updateRequest.getUserId());
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found.");
            }

            if (updateRequest.getStoreName() != null && !updateRequest.getStoreName().trim().isEmpty()) {
                userService.validateStoreName(updateRequest.getStoreName());
                user.setStoreName(updateRequest.getStoreName());
            }

            if (updateRequest.getCategory() != null && !updateRequest.getCategory().trim().isEmpty()) {
                StoreCategory category = storeCategoryService.findByName(updateRequest.getCategory());
                if (category != null) {
                    user.setCategory(category);
                } else {
                    return ResponseEntity.badRequest().body("Invalid category.");
                }
            }

            if (updateRequest.getPackageType() != null) {
                user.setPackageType(updateRequest.getPackageType());
                user.initializePlugins();
            }

            if (updateRequest.getPassword() != null && !updateRequest.getPassword().trim().isEmpty()) {
                userService.validatePassword(updateRequest.getPassword());
                user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
            }

            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating profile");
        }
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
    public ResponseEntity<?> deleteAccount(@RequestParam String userId) {
        boolean success = userService.deleteAccountById(userId);
        if (success) {
            return ResponseEntity.ok("Account has been deleted.");
        } else {
            return ResponseEntity.badRequest().body("User not found.");
        }
    }
}
