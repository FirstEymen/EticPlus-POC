package com.EticPlus_POC.utility;

import com.EticPlus_POC.models.User;

public class UserRegistrationRequest {

    private String storeName;
    private String category;
    private String password;
    private User.PackageType packageType;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User.PackageType getPackageType() {
        return packageType;
    }

    public void setPackageType(User.PackageType packageType) {
        this.packageType = packageType;
    }
}
