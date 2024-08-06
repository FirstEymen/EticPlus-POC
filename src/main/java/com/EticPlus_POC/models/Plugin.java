package com.EticPlus_POC.models;

public class Plugin {
    private final String name;
    private boolean isActive;

    public Plugin(String name, boolean isActive) {
        this.name = name;
        this.isActive = isActive;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
