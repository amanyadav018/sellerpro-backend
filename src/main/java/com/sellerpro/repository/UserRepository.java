package com.sellerpro.repository;

import com.sellerpro.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);

    List<User> findByRole(User.Role role);

    @Query("SELECT u FROM User u WHERE u.isActive = true ORDER BY u.createdAt DESC")
    List<User> findAllActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'SELLER' AND u.isActive = true")
    Long countActiveSellerUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    Long countNewUsersSince(LocalDateTime since);
}
