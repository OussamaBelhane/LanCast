package com.lancast.lancast.models;

/**
 * Represents a user account in the LanCast application.
 */
public class User {
    private int id;
    private String username;
    private String createdAt;

    public User(int id, String username, String createdAt) {
        this.id = id;
        this.username = username;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', createdAt='" + createdAt + "'}";
    }
}
