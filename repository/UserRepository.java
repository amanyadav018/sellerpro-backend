package com.sellerpro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * UserRepository — extend with your actual User entity.
 * Replace `User` and `Long` with your project's User model types.
 *
 * Example if your entity is:
 *   @Entity @Table(name = "users") public class User { @Id Long id; String email; ... }
 */
public interface UserRepository extends JpaRepository<Object, Long> {

    long count();

    @Query(value = """
        SELECT u.email, u.name,
               COALESCE(p.name, 'BASIC') as plan,
               s.status as subscriptionStatus,
               s.end_date as endDate
        FROM users u
        LEFT JOIN subscriptions s ON s.user_email = u.email
            AND s.status = 'ACTIVE'
            AND s.id = (SELECT MAX(s2.id) FROM subscriptions s2
                        WHERE s2.user_email = u.email AND s2.status = 'ACTIVE')
        LEFT JOIN plans p ON p.id = s.plan_id
        WHERE (:search = '' OR u.email ILIKE '%' || :search || '%'
               OR u.name ILIKE '%' || :search || '%')
        ORDER BY u.created_at DESC
        """, nativeQuery = true)
    Page<Object[]> searchCustomers(@Param("search") String search, Pageable pageable);
}
