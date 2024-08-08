package com.EticPlus_POC.dto;

public class UserProfileResponse {
    private String storeName;
    private String password;
    private String category;
    private String packageType;

    public UserProfileResponse(String storeName, String password, String category, String packageType) {
        this.storeName = storeName;
        this.password = password;
        this.category = category;
        this.packageType = packageType;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }
}
