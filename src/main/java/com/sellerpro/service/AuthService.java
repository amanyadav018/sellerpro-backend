package com.sellerpro.service;

import com.sellerpro.dto.AuthResponse;
import com.sellerpro.dto.LoginRequest;
import com.sellerpro.dto.RegisterRequest;
import com.sellerpro.entity.Client;
import com.sellerpro.entity.Subscription;
import com.sellerpro.entity.User;
import com.sellerpro.repository.ClientRepository;
import com.sellerpro.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final ClientRepository clientRepository;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase().trim(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userService.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("User not found"));

        userService.updateLastLogin(user.getId());

        String token = jwtUtil.generateToken(user);
        return buildAuthResponse(user, token);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = userService.registerUser(request);

        // Send verification email
        try {
            emailService.sendVerificationEmail(user.getEmail(), user.getFullName(),
                    user.getEmailVerificationToken());
        } catch (Exception e) {
            log.warn("Failed to send verification email to {}: {}", user.getEmail(), e.getMessage());
        }

        String token = jwtUtil.generateToken(user);
        return buildAuthResponse(user, token);
    }

    public void forgotPassword(String email) {
        try {
            String resetToken = userService.generatePasswordResetToken(email);
            User user = userService.findByEmail(email).orElseThrow();
            emailService.sendPasswordResetEmail(email, user.getFullName(), resetToken);
        } catch (Exception e) {
            // Don't reveal if email exists
            log.warn("Forgot password request for unknown email: {}", email);
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        userService.resetPassword(token, newPassword);
    }

    @Transactional
    public void verifyEmail(String token) {
        userService.verifyEmail(token);
    }

    public boolean emailExists(String email) {
        return userService.emailExists(email);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        Subscription activeSub = userService.getActiveSubscription(user.getId());
        Optional<Client> client = clientRepository.findByUserId(user.getId());

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .emailVerified(user.getEmailVerified())
                .onboardingComplete(client.map(Client::getOnboardingComplete).orElse(false))
                .planName(activeSub != null ? activeSub.getPlan().getName() : null)
                .planDisplayName(activeSub != null ? activeSub.getPlan().getDisplayName() : null)
                .subscriptionStatus(activeSub != null ? activeSub.getStatus().name() : "NO_PLAN")
                .subscriptionEndDate(activeSub != null ? activeSub.getEndDate().toString() : null)
                .build();

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationMs() / 1000)
                .user(userInfo)
                .build();
    }
}
