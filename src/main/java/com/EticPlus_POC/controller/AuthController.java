package com.EticPlus_POC.controller;

import com.EticPlus_POC.dto.UserProfileResponse;
import com.EticPlus_POC.dto.UserUpdateRequest;
import com.EticPlus_POC.exception.BusinessException;
import com.EticPlus_POC.models.ActionLog;
import com.EticPlus_POC.models.Plugin;
import com.EticPlus_POC.models.StoreCategory;
import com.EticPlus_POC.models.User;
import com.EticPlus_POC.repository.ActionLogRepository;
import com.EticPlus_POC.service.StoreCategoryService;
import com.EticPlus_POC.service.UserService;
import com.EticPlus_POC.utility.AuthenticationRequest;
import com.EticPlus_POC.utility.AuthenticationResponse;
import com.EticPlus_POC.utility.JwtUtil;
import com.EticPlus_POC.utility.UserRegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private StoreCategoryService storeCategoryService;
    @Autowired
    private ActionLogRepository actionLogRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest request) {
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
        try {
            String jwt = userService.authenticateUser(authenticationRequest);
            return ResponseEntity.ok(new AuthenticationResponse(jwt));
        } catch (UsernameNotFoundException e) {
            throw new BusinessException("USER_NOT_FOUND", "User not found");
        }
    }

    @GetMapping("/home")
    public ResponseEntity<?> getHomePage(@RequestHeader("Authorization") String authorizationHeader) {
        User user = userService.getUserFromToken(authorizationHeader);
        List<Plugin> plugins = user.getPlugins();
        return ResponseEntity.ok(plugins);
    }

    @PostMapping("/togglePlugin")
    public ResponseEntity<?> togglePlugin(@RequestHeader("Authorization") String authorizationHeader, @RequestParam String pluginName) {
        User user = userService.getUserFromToken(authorizationHeader);
        userService.togglePlugin(user, pluginName);
        return ResponseEntity.ok("Plugin status updated.");
    }

    @GetMapping("/categories")
    public ResponseEntity<List<StoreCategory>> getAllCategories() {
        List<StoreCategory> categories = storeCategoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authorizationHeader) {
        User user = userService.getUserFromToken(authorizationHeader);
        UserProfileResponse profileResponse = new UserProfileResponse(user.getStoreName(), user.getPassword(), user.getCategory().getName(), String.valueOf(user.getPackageType()));
        return ResponseEntity.ok(profileResponse);
    }

    @PutMapping("/updateProfile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authorizationHeader, @RequestBody UserUpdateRequest updateRequest) {
        User user = userService.getUserFromToken(authorizationHeader);
        boolean isUpdated = userService.updateUserProfile(user, updateRequest);
        return ResponseEntity.ok(isUpdated ? "Profile updated successfully." : "No updates made.");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("You have been successfully logged out.");
    }

    @PostMapping("/deleteAccount")
    public ResponseEntity<?> deleteAccount(@RequestHeader("Authorization") String authorizationHeader) {
        User user = userService.getUserFromToken(authorizationHeader);
        boolean success = userService.deleteAccount(user.getId());
        return ResponseEntity.ok(success ? "Account has been deleted." : "Error deleting account.");
    }

    @GetMapping("/actions")
    public ResponseEntity<List<ActionLog>> getAllActions() {
        List<ActionLog> actions = actionLogRepository.findAll();
        return ResponseEntity.ok(actions);
    }

    @GetMapping("/actions/{storeName}")
    public ResponseEntity<List<ActionLog>> getActionsByStoreName(@PathVariable String storeName) {
        List<ActionLog> actions = actionLogRepository.findByStoreName(storeName);
        return ResponseEntity.ok(actions);
    }

}
