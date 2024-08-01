package com.EticPlus_POC.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "platformUser")
public class User {

    @Id
    private String id;
    @Indexed(unique = true)
    private String storeName;
    private StoreCategory category;
    private String password;
    private PackageType packageType;
    private List<Plugin> plugins = new ArrayList<>();

    public User() {}

    public User(String storeName, StoreCategory category, String password, PackageType packageType) {
        this.storeName = storeName;
        this.category = category;
        this.password = password;
        this.packageType = packageType;
        initializePlugins();
    }

    private void initializePlugins() {
        plugins.add(new Plugin("Benim Sayfam", true));
        plugins.add(new Plugin("Günlük Satış Raporu", false));
        plugins.add(new Plugin("Google Analytics", false));
        plugins.add(new Plugin("Chatmate", false));
        plugins.add(new Plugin("ReviewMe", false));
        plugins.add(new Plugin("GiftSend", false));

        if (packageType == PackageType.PLATINUM) {
            plugins.forEach(plugin -> plugin.setActive(true));
        }
    }

    // Getter ve Setter metodları

    public List<Plugin> getPlugins() {
        return plugins;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public StoreCategory getCategory() {
        return category;
    }

    public void setCategory(StoreCategory category) {
        this.category = category;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public PackageType getPackageType() {
        return packageType;
    }

    public void setPackageType(PackageType packageType) {
        this.packageType = packageType;
    }

    public enum PackageType {
        SILVER, GOLD, PLATINUM
    }
}
