package com.lancast.lancast.dao;

import com.lancast.lancast.model.TransferLog;
import java.util.List;

/**
 * Data Access Object interface for TransferLog entities.
 * Provides CRUD operations for transfer history records.
 */
public interface TransferLogDAO {

    /**
     * Creates a new transfer log entry.
     */
    void create(String clientIp, String fileName, String deviceType, int userId);

    /**
     * Retrieves all transfer logs for a specific user.
     */
    List<TransferLog> findByUserId(int userId);

    /**
     * Retrieves all transfer logs.
     */
    List<TransferLog> findAll();

    /**
     * Deletes all transfer logs.
     */
    void deleteAll();

    /**
     * Deletes all transfer logs for a specific user.
     */
    void deleteByUserId(int userId);
}
