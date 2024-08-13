package com.EticPlus_POC.dto;

import com.EticPlus_POC.models.User;

public class UserUpdateRequest {
    private String storeName;
    private String category;
    private User.PackageType packageType;
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;

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

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }
}
