package com.EticPlus_POC.dto;

import com.EticPlus_POC.models.User;

public class UserUpdateRequest {
    private String storeName;
    private String category;
    private User.PackageType packageType;
    private String password;
    private String confirmPassword;

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public User.PackageType getPackageType() {
        return packageType;
    }

    public void setPackageType(User.PackageType packageType) {
        this.packageType = packageType;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
