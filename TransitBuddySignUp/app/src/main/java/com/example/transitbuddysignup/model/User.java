package com.example.transitbuddysignup.model;

import org.bson.types.ObjectId;

public class User {
    private ObjectId id;
    private String email;
    private String fullName;
    private String passwordHash;
    private String salt;
    private long createdAt;

    // Default constructor for MongoDB
    public User() {
    }

    // Constructor for new user creation
    public User(String email, String fullName, String passwordHash, String salt) {
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
} 