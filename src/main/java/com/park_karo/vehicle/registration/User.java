package com.park_karo.vehicle.registration;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_registrations") // Matches your collection name
public class User {
    // MongoDB auto-generates this field
    @Id
    private String id;

    // Application-generated unique ID (UUID)
    private String userId;

    private String name;
    private String email;
    private String passwordHash; // Store only the hash, never plain password
    private String phoneNumber;

    public User(String id, String userId, String name, String email, String passwordHash,
            String phoneNumber) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "park_karomyresgiatrationuser [userId=" + userId + ", name=" + name + ", email=" + email
                + ", passwordHash=" + passwordHash + ", phoneNumber=" + phoneNumber + "]";
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    
}
