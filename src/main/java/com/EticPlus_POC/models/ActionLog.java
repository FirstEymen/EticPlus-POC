package com.EticPlus_POC.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "actionLog")
public class ActionLog {

    @Id
    private String id;
    private String storeName;
    private String action;
    private LocalDateTime timestamp;
    private String userId;
    private String actionDetails;

    public ActionLog(String storeName, String action, LocalDateTime timestamp, String userId, String actionDetails) {
        this.storeName = storeName;
        this.action = action;
        this.timestamp = timestamp;
        this.userId = userId;
        this.actionDetails = actionDetails;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActionDetails() {
        return actionDetails;
    }

    public void setActionDetails(String actionDetails) {
        this.actionDetails = actionDetails;
    }
}
