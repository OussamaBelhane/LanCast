package com.lancast.lancast.dao;

import com.lancast.lancast.model.User;

/**
 * Data Access Object interface for User entities.
 * Provides CRUD operations for user accounts.
 */
public interface UserDAO {

    /**
     * Creates a new user account.
     * 
     * @return true if created successfully, false if username exists
     */
    boolean create(String username, String passwordHash);

    /**
     * Finds a user by username.
     * 
     * @return User object or null if not found
     */
    User findByUsername(String username);

    /**
     * Finds a user by ID.
     * 
     * @return User object or null if not found
     */
    User findById(int id);

    /**
     * Validates user credentials.
     * 
     * @return User object if valid, null otherwise
     */
    User authenticate(String username, String passwordHash);

    /**
     * Deletes a user by ID.
     */
    void deleteById(int id);
}
