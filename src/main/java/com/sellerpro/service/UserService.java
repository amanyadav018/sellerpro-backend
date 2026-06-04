package com.sellerpro.service;

import com.sellerpro.dto.RegisterRequest;
import com.sellerpro.entity.Client;
import com.sellerpro.entity.Subscription;
import com.sellerpro.entity.SubscriptionStatus;
import com.sellerpro.entity.User;
import com.sellerpro.repository.ClientRepository;
import com.sellerpro.repository.SubscriptionRepository;
import com.sellerpro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(User.Role.SELLER)
                .isActive(true)
                .emailVerified(false)
                .emailVerificationToken(UUID.randomUUID().toString())
                .build();

        user = userRepository.save(user);

        // Create empty client profile
        Client client = Client.builder()
                .user(user)
                .onboardingComplete(false)
                .build();
        clientRepository.save(client);

        log.info("New user registered: {}", user.getEmail());
        return user;
    }

    @Transactional
    public void updateLastLogin(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification token"));
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);
    }

    @Transactional
    public String generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email"));
        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetExpires(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Password reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public Subscription getActiveSubscription(Long userId) {
        return subscriptionRepository
                .findActiveSubscription(userId, LocalDate.now())
                .orElse(null);
    }
}
