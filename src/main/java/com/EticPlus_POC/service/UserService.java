package com.EticPlus_POC.service;

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
            throw new IllegalArgumentException("Store name already exists.");
        }

        if (userRepository.findAll().stream().anyMatch(u -> passwordEncoder.matches(user.getPassword(), u.getPassword()))) {
            throw new IllegalArgumentException("This password is already in use. Please choose a different password.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        setDefaultPlugins(user);
        return userRepository.save(user);
    }

    public void validateStoreName(String storeName) {
        if (storeName == null || storeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Store name cannot be empty.");
        }
        if (storeName.length() < 3) {
            throw new IllegalArgumentException("Store name must be at least 3 characters long.");
        }
        if (storeName.length() > 20) {
            throw new IllegalArgumentException("Store name cannot be more than 20 characters long.");
        }
        if (storeName.startsWith(" ")) {
            throw new IllegalArgumentException("Store name cannot start with a space.");
        }
        if (storeName.contains("  ")) {
            throw new IllegalArgumentException("Store name cannot contain consecutive spaces.");
        }
        if (!storeName.matches("^[a-zA-Z0-9 ]*$")) {
            throw new IllegalArgumentException("Store name cannot contain special characters.");
        }
    }

    public void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        if (password.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters long.");
        }
        if (password.length() > 15) {
            throw new IllegalArgumentException("Password cannot be more than 15 characters long.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter.");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Password must contain at least one digit.");
        }
    }

    private void setDefaultPlugins(User user) {
        user.getPlugins().add(new Plugin("Benim Sayfam", true));
        user.getPlugins().add(new Plugin("Günlük Satış Raporu", false));
        user.getPlugins().add(new Plugin("Google Analytics", false));
        user.getPlugins().add(new Plugin("Chatmate", false));
        user.getPlugins().add(new Plugin("ReviewMe", false));
        user.getPlugins().add(new Plugin("GiftSend", false));

        if (user.getPackageType() == User.PackageType.PLATINUM) {
            user.getPlugins().forEach(plugin -> plugin.setActive(true));
        }
    }

    public User findById(String userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.orElse(null);
    }

    public void togglePlugin(User user, String pluginName) {
        long activePluginsCount = user.getPlugins().stream().filter(Plugin::isActive).count();

        user.getPlugins().forEach(plugin -> {
            if (plugin.getName().equals(pluginName)) {
                if (user.getPackageType() == User.PackageType.PLATINUM || (!plugin.isActive() && activePluginsCount < 3) || plugin.isActive()) {
                    plugin.setActive(!plugin.isActive());
                    System.out.println("Mağaza " + user.getStoreName() + ", " + pluginName + " isimli eklentiyi " + (plugin.isActive() ? "aktif" : "deaktif") + " etti.");
                }
            }
        });

        userRepository.save(user);
    }

    public boolean deleteAccountById(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }
}
