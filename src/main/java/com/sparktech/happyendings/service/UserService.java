package com.sparktech.happyendings.service;

import com.sparktech.happyendings.dto.RegisterRequest;
import com.sparktech.happyendings.model.User;
import com.sparktech.happyendings.model.enums.Role;
import com.sparktech.happyendings.model.enums.AccountStatus;
import com.sparktech.happyendings.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActionLogService actionLogService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisService redisService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email address already in use.");
        }

        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.REGISTERED_USER);
        user.setAccountStatus(AccountStatus.ACTIVE);

        // Map name to first and last names as fallback
        if (registerRequest.getName() != null) {
            String[] parts = registerRequest.getName().split(" ", 2);
            user.setFirstName(parts[0]);
            if (parts.length > 1) {
                user.setLastName(parts[1]);
            }
        }

        User newUser = userRepository.save(user);

        // Audit & Kafka
        actionLogService.logAction(newUser.getId(), "USER_CREATED", "New user registered with email: " + user.getEmail());
        kafkaProducerService.sendEvent("user-events", "UserRegistered:" + newUser.getId());

        // Proactively generate verification token
        generateEmailVerification(newUser.getEmail());

        return newUser;
    }

    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User updateUserProfile(Long userId, String firstName, String lastName, String phoneNumber, String profilePhoto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phoneNumber);
        if (profilePhoto != null) {
            user.setProfilePhoto(profilePhoto);
        }
        user.setName((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""));

        User savedUser = userRepository.save(user);
        actionLogService.logAction(userId, "USER_PROFILE_UPDATED", "User profile details updated.");
        kafkaProducerService.sendEvent("user-events", "UserProfileUpdated:" + userId);
        return savedUser;
    }

    @Transactional
    public void changeAccountStatus(Long userId, AccountStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        user.setAccountStatus(status);
        userRepository.save(user);

        actionLogService.logAction(userId, "USER_STATUS_CHANGED", "User account status set to: " + status);
        kafkaProducerService.sendEvent("user-events", "UserStatusChanged:" + userId + ":" + status);
    }

    public String generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No user found with email: " + email));

        String token = UUID.randomUUID().toString();
        // Store in Redis with 15 min TTL
        redisService.set("reset:token:" + token, email, 900);

        actionLogService.logAction(user.getId(), "PASSWORD_RESET_REQUESTED", "Password reset token requested.");
        kafkaProducerService.sendEvent("user-events", "PasswordResetRequested:" + email);
        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        String email = redisService.get("reset:token:" + token, String.class);
        if (email == null) {
            throw new IllegalArgumentException("Invalid or expired password reset token.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redisService.delete("reset:token:" + token);

        actionLogService.logAction(user.getId(), "PASSWORD_RESET_COMPLETED", "Password successfully changed.");
        kafkaProducerService.sendEvent("user-events", "PasswordResetCompleted:" + user.getId());
    }

    public String generateEmailVerification(String email) {
        String token = UUID.randomUUID().toString();
        // Store in Redis with 24 hours TTL (86400 seconds)
        redisService.set("verify:token:" + token, email, 86400);

        kafkaProducerService.sendEvent("user-events", "EmailVerificationRequested:" + email);
        return token;
    }

    @Transactional
    public void verifyEmail(String token) {
        String email = redisService.get("verify:token:" + token, String.class);
        if (email == null) {
            throw new IllegalArgumentException("Invalid or expired email verification token.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        redisService.delete("verify:token:" + token);

        actionLogService.logAction(user.getId(), "EMAIL_VERIFIED", "Email address verified successfully.");
        kafkaProducerService.sendEvent("user-events", "EmailVerified:" + user.getId());
    }
}
