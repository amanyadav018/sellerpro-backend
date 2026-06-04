package com.sellerpro.repository;

import com.sellerpro.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT u.email FROM User u JOIN u.subscriptions s JOIN s.plan p " +
           "WHERE p.name = :planName AND s.status = 'ACTIVE' AND s.endDate >= CURRENT_DATE " +
           "AND p.whatsappDigest = true")
    List<String> findEmailsByPlanAndWhatsappEnabled(@Param("planName") String planName);

    @Query("SELECT u.phone FROM User u WHERE u.email = :email")
    String findPhoneByEmail(@Param("email") String email);

    @Query("SELECT p.name FROM User u JOIN u.subscriptions s JOIN s.plan p " +
           "WHERE u.email = :email AND s.status = 'ACTIVE' AND s.endDate >= CURRENT_DATE")
    String findPlanByEmail(@Param("email") String email);
}
