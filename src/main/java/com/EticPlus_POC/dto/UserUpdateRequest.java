package com.EticPlus_POC.dto;

import com.EticPlus_POC.models.User;

public class UserUpdateRequest {
    private String userId;
    private String storeName;
    private String password;
    private String category;
    private User.PackageType packageType;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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
}
