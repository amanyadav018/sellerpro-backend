package com.sellerpro.service;

import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * UserService — user management operations used by AdminController.
 * Implement this in your existing UserService or create a new bean.
 */
public interface UserService {

    /**
     * Search customers by name or email with pagination.
     * Returned page should contain maps with keys:
     * email, name, plan, subscriptionStatus, endDate
     */
    Page<Map<String, Object>> searchCustomers(String search, int page, int size);

    /**
     * Get detailed info for one customer (subscription history, logs, etc.)
     */
    Map<String, Object> getCustomerDetail(String email);

    /**
     * Hard-delete a user by email (admin only).
     */
    void deleteUser(String email);
}
