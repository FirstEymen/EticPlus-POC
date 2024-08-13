package com.EticPlus_POC.service;

import com.EticPlus_POC.dto.UserUpdateRequest;
import com.EticPlus_POC.exception.BusinessException;
import com.EticPlus_POC.models.ActionLog;
import com.EticPlus_POC.models.Plugin;
import com.EticPlus_POC.models.StoreCategory;
import com.EticPlus_POC.models.User;
import com.EticPlus_POC.repository.ActionLogRepository;
import com.EticPlus_POC.repository.UserRepository;
import com.EticPlus_POC.utility.AuthenticationRequest;
import com.EticPlus_POC.utility.JwtUtil;
import com.EticPlus_POC.utility.UserRegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private StoreCategoryService storeCategoryService;
    @Autowired
    private ActionLogRepository actionLogRepository;

    public User registerUser(UserRegistrationRequest request) {
        validateStoreName(request.getStoreName());
        validatePassword(request.getPassword());

        StoreCategory category = storeCategoryService.findByName(request.getCategory());
        if (category == null) {
            throw new BusinessException("INVALID_CATEGORY", "Invalid category");
        }
        if (request.getPackageType() == null) {
            throw new BusinessException("PACKAGE_NOT_SELECTED", "Package type must be selected.");
        }

        User user = new User(request.getStoreName(), category, request.getPassword(), request.getPackageType());
        if (userRepository.findByStoreName(user.getStoreName()).isPresent()) {
            throw new BusinessException("STORE_NAME_EXISTS", "Store name already exists.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        logAction(request.getStoreName(), "User Registered", savedUser.getId(), "User registered with package type: " + request.getPackageType());
        return savedUser;
    }

    public String authenticateUser(AuthenticationRequest authenticationRequest) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getStoreName());
        if (passwordEncoder.matches(authenticationRequest.getPassword(), userDetails.getPassword())) {
            return jwtUtil.generateToken(userDetails);
        } else {
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid credentials");
        }
    }

    public User getUserFromToken(String authorizationHeader) {
        validateAuthorizationHeader(authorizationHeader);
        String username = jwtUtil.extractUsername(getJwtToken(authorizationHeader));
        return findByStoreName(username).orElseThrow(() ->
                new BusinessException("USER_NOT_FOUND", "User not found"));
    }

    public boolean updateUserProfile(User user, UserUpdateRequest updateRequest) {
        boolean isUpdated = false;

        StringBuilder actionDetails = new StringBuilder("User profile updated with the following changes:");
        if (updateRequest.getCurrentPassword() != null && !updateRequest.getCurrentPassword().trim().isEmpty()) {
            if (!passwordEncoder.matches(updateRequest.getCurrentPassword(), user.getPassword())) {
                throw new BusinessException("INVALID_CURRENT_PASSWORD", "Current password is incorrect.");
            }
            if (updateRequest.getNewPassword() != null && !updateRequest.getNewPassword().trim().isEmpty()) {
                if (updateRequest.getNewPassword().equals(updateRequest.getConfirmNewPassword())) {
                    validatePassword(updateRequest.getNewPassword());
                    user.setPassword(passwordEncoder.encode(updateRequest.getNewPassword()));
                    actionDetails.append(" Password changed.");
                    isUpdated = true;
                } else {
                    throw new BusinessException("PASSWORD_MISMATCH", "New passwords do not match.");
                }
            }
        }
        if (updateRequest.getStoreName() != null && !updateRequest.getStoreName().trim().isEmpty() &&
                !updateRequest.getStoreName().equals(user.getStoreName())) {
            Optional<User> existingUser = userRepository.findByStoreName(updateRequest.getStoreName());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                throw new BusinessException("STORE_NAME_EXISTS", "Store name already exists.");
            }
            validateStoreName(updateRequest.getStoreName());
            user.setStoreName(updateRequest.getStoreName());
            actionDetails.append(" Store name changed to '").append(updateRequest.getStoreName()).append("'.");
            isUpdated = true;
        }
        if (updateRequest.getCategory() != null && !updateRequest.getCategory().trim().isEmpty() &&
                !updateRequest.getCategory().equals(user.getCategory().getName())) {
            StoreCategory category = storeCategoryService.findByName(updateRequest.getCategory());
            if (category != null) {
                user.setCategory(category);
                actionDetails.append(" Category changed to '").append(updateRequest.getCategory()).append("'.");
                isUpdated = true;
            } else {
                throw new BusinessException("INVALID_CATEGORY", "Invalid category.");
            }
        }
        if (updateRequest.getPackageType() != null && !updateRequest.getPackageType().equals(user.getPackageType())) {
            user.setPackageType(updateRequest.getPackageType());
            user.initializePlugins();
            actionDetails.append(" Package type changed to '").append(updateRequest.getPackageType()).append("'.");
            isUpdated = true;
        }
        if (isUpdated) {
            userRepository.save(user);
            logAction(user.getStoreName(), "Profile Updated", user.getId(), actionDetails.toString());
        }
        return isUpdated;
    }

    public User updateUser(User user) {
        validateStoreName(user.getStoreName());
        validatePassword(user.getPassword());
        return userRepository.save(user);
    }

    public boolean deleteAccount(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            userRepository.deleteById(userId);
            logAction(user.get().getStoreName(), "Account Deleted", userId, "User account deleted");
            return true;
        } else {
            throw new BusinessException("USER_NOT_FOUND", "User not found.");
        }
    }

    public Optional<User> findByStoreName(String storeName) {
        return userRepository.findByStoreName(storeName);
    }

    public void togglePlugin(User user, String pluginName) {
        long activePluginsCount = user.getPlugins().stream().filter(Plugin::isActive).count();

        user.getPlugins().forEach(plugin -> {
            if (plugin.getName().equals(pluginName)) {
                if ("My Page".equals(pluginName)) {
                    return;
                }
                boolean canToggle = user.getPackageType() == User.PackageType.PLATINUM ||
                        (!plugin.isActive() && activePluginsCount < 4) || plugin.isActive();
                if (canToggle) {
                    plugin.setActive(!plugin.isActive());
                    System.out.println("Store " + user.getStoreName() + " has " + (plugin.isActive() ? "activated" : "deactivated") + " the plugin named " + pluginName + ".");
                    userRepository.save(user);
                    logAction(user.getStoreName(), "Plugin toggled: " + pluginName, user.getId(), "Plugin status changed to " + (plugin.isActive() ? "active" : "inactive"));
                } else {
                    throw new BusinessException("PLUGIN_LIMIT_EXCEEDED", "Cannot activate more plugins.");
                }
            }
        });
    }

    private void validateStoreName(String storeName) {
        if (storeName == null || storeName.trim().isEmpty()) {
            throw new BusinessException("INVALID_STORE_NAME", "Store name cannot be empty.");
        }
        if (storeName.length() < 3) {
            throw new BusinessException("INVALID_STORE_NAME", "Store name must be at least 3 characters long.");
        }
        if (storeName.length() > 20) {
            throw new BusinessException("INVALID_STORE_NAME", "Store name cannot be more than 20 characters long.");
        }
        if (storeName.startsWith(" ") || storeName.endsWith(" ")) {
            throw new BusinessException("INVALID_STORE_NAME", "Store name cannot start or end with a space.");
        }
        if (storeName.contains("  ")) {
            throw new BusinessException("INVALID_STORE_NAME", "Store name cannot contain consecutive spaces.");
        }
        if (!storeName.matches("^[a-zA-Z0-9 ]*$")) {
            throw new BusinessException("INVALID_STORE_NAME", "Store name cannot contain special characters.");
        }
        if (storeName.matches(".*[çÇşŞğĞüÜöÖıİâÂîÎ].*")) {
            throw new BusinessException("INVALID_STORE_NAME", "Store name cannot contain Turkish characters.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException("INVALID_PASSWORD", "Password cannot be empty.");
        }
        if (password.length() < 4) {
            throw new BusinessException("INVALID_PASSWORD", "Password must be at least 4 characters long.");
        }
        if (password.length() > 15) {
            throw new BusinessException("INVALID_PASSWORD", "Password cannot be more than 15 characters long.");
        }
        if (password.contains(" ")) {
            throw new BusinessException("INVALID_PASSWORD", "Password cannot contain spaces.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new BusinessException("INVALID_PASSWORD", "Password must contain at least one uppercase letter.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new BusinessException("INVALID_PASSWORD", "Password must contain at least one lowercase letter.");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new BusinessException("INVALID_PASSWORD", "Password must contain at least one digit.");
        }
        if (!password.matches("^[a-zA-Z0-9]*$")) {
            throw new BusinessException("INVALID_PASSWORD", "Password cannot contain special characters.");
        }
        if (password.matches(".*[çÇşŞğĞüÜöÖıİâÂîÎ].*")) {
            throw new BusinessException("INVALID_PASSWORD", "Password cannot contain Turkish characters.");
        }
    }

    private void validateAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BusinessException("INVALID_TOKEN", "Invalid authorization header.");
        }
    }

    private String getJwtToken(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }
    public void logAction(String storeName, String action, String userId, String actionDetails) {
        ActionLog actionLog = new ActionLog(storeName, action, LocalDateTime.now(), userId, actionDetails);
        actionLogRepository.save(actionLog);
    }

}
