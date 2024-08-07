package com.EticPlus_POC.service;

import com.EticPlus_POC.exception.BusinessException;
import com.EticPlus_POC.models.Plugin;
import com.EticPlus_POC.models.User;
import com.EticPlus_POC.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        validateStoreName(user.getStoreName());
        validatePassword(user.getPassword());

        if (userRepository.findByStoreName(user.getStoreName()).isPresent()) {
            throw new BusinessException("STORE_NAME_EXISTS", "Store name already exists.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public void validateStoreName(String storeName) {
        if (storeName == null || storeName.trim().isEmpty()) {
            throw new BusinessException("EMPTY_STORE_NAME", "Store name cannot be empty.");
        }
        if (storeName.length() < 3) {
            throw new BusinessException("STORE_NAME_TOO_SHORT", "Store name must be at least 3 characters long.");
        }
        if (storeName.length() > 20) {
            throw new BusinessException("STORE_NAME_TOO_LONG", "Store name cannot be more than 20 characters long.");
        }
        if (storeName.startsWith(" ")) {
            throw new BusinessException("STORE_NAME_STARTS_WITH_SPACE", "Store name cannot start with a space.");
        }
        if (storeName.contains("  ")) {
            throw new BusinessException("STORE_NAME_CONTAINS_CONSECUTIVE_SPACES", "Store name cannot contain consecutive spaces.");
        }
        if (!storeName.matches("^[a-zA-Z0-9 ]*$")) {
            throw new BusinessException("STORE_NAME_INVALID_CHARACTERS", "Store name cannot contain special characters.");
        }
        if (storeName.matches(".*[çÇşŞğĞüÜöÖıİâÂîÎ].*")) {
            throw new BusinessException("STORE_NAME_TURKISH_CHARACTERS", "Store name cannot contain Turkish characters.");
        }
    }

    public void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException("EMPTY_PASSWORD", "Password cannot be empty.");
        }
        if (password.length() < 4) {
            throw new BusinessException("PASSWORD_TOO_SHORT", "Password must be at least 4 characters long.");
        }
        if (password.length() > 15) {
            throw new BusinessException("PASSWORD_TOO_LONG", "Password cannot be more than 15 characters long.");
        }
        if (password.contains(" ")) {
            throw new BusinessException("PASSWORD_CONTAINS_SPACES", "Password cannot contain spaces.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new BusinessException("PASSWORD_MISSING_UPPERCASE", "Password must contain at least one uppercase letter.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new BusinessException("PASSWORD_MISSING_LOWERCASE", "Password must contain at least one lowercase letter.");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new BusinessException("PASSWORD_MISSING_DIGIT", "Password must contain at least one digit.");
        }
        if (!password.matches("^[a-zA-Z0-9]*$")) {
            throw new BusinessException("PASSWORD_INVALID_CHARACTERS", "Password cannot contain special characters.");
        }
        if (password.matches(".*[çÇşŞğĞüÜöÖıİâÂîÎ].*")) {
            throw new BusinessException("PASSWORD_TURKISH_CHARACTERS", "Password cannot contain Turkish characters.");
        }
    }

    public User findById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public void togglePlugin(User user, String pluginName) {
        long activePluginsCount = user.getPlugins().stream().filter(Plugin::isActive).count();

        user.getPlugins().forEach(plugin -> {
            if (plugin.getName().equals(pluginName)) {
                if ("Benim Sayfam".equals(pluginName)) {
                    return;
                }
                boolean canToggle = user.getPackageType() == User.PackageType.PLATINUM ||
                        (!plugin.isActive() && activePluginsCount < 3) || plugin.isActive();
                if (canToggle) {
                    plugin.setActive(!plugin.isActive());
                    System.out.println("Mağaza " + user.getStoreName() + ", " + pluginName + " isimli eklentiyi " + (plugin.isActive() ? "aktif" : "deaktif") + " etti.");
                } else {
                    throw new BusinessException("PLUGIN_LIMIT_EXCEEDED", "Up to 3 plugins can be activated for Silver and Gold packages.");
                }
            }
        });
        userRepository.save(user);
    }

    public User updateUser(User user) {
        validateStoreName(user.getStoreName());
        validatePassword(user.getPassword());
        return userRepository.save(user);
    }

    public boolean deleteAccountById(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    public Optional<User> findByStoreName(String storeName) {
        return userRepository.findByStoreName(storeName);
    }
}
