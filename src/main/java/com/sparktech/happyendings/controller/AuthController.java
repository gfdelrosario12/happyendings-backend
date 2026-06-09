package com.sparktech.happyendings.controller;

import com.sparktech.happyendings.dto.ApiResponse;
import com.sparktech.happyendings.dto.AuthResponse;
import com.sparktech.happyendings.dto.LoginRequest;
import com.sparktech.happyendings.dto.RegisterRequest;
import com.sparktech.happyendings.dto.UserDto;
import com.sparktech.happyendings.model.User;
import com.sparktech.happyendings.service.AuthService;
import com.sparktech.happyendings.service.UserService;
import com.sparktech.happyendings.service.ActionLogService;
import com.sparktech.happyendings.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private ActionLogService actionLogService;

    @Autowired
    private RedisService redisService;

    /**
     * Authenticates a user and returns a JWT upon success.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            log.info("Attempting login for user email: {}", loginRequest.getEmail());
            
            String jwt = authService.authenticateUser(loginRequest);
            
            // Log successful login
            actionLogService.logAction(null, "LOGIN_SUCCESS", "User logged in successfully with email: " + loginRequest.getEmail());
            log.info("Login successful for user email: {}", loginRequest.getEmail());
            
            return ResponseEntity.ok(ApiResponse.success(new AuthResponse(jwt)));
        } catch (AuthenticationException ex) {
            // Log failed login attempt
            actionLogService.logAction(null, "LOGIN_FAILED", "Failed login attempt for email: " + loginRequest.getEmail());
            log.warn("Login failed for user email: {}. Reason: {}", loginRequest.getEmail(), ex.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid credentials"));
        }
    }

    /**
     * Registers a new user account.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            log.info("Attempting registration for email: {}", registerRequest.getEmail());
            User newUser = userService.registerUser(registerRequest);
            
            UserDto userDto = new UserDto(
                    newUser.getId(),
                    newUser.getName(),
                    newUser.getEmail(),
                    newUser.getGender(),
                    newUser.getAge(),
                    newUser.getRole() != null ? newUser.getRole().name() : null
            );
            
            log.info("Registration successful for email: {}", registerRequest.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(userDto));
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed (bad request): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Registration failed due to server error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Registration failed"));
        }
    }

    /**
     * Retrieves the currently authenticated user's details.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("User not authenticated"));
        }

        String email;
        if (authentication.getPrincipal() instanceof UserDetails) {
            email = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            email = authentication.getPrincipal().toString();
        }

        Optional<User> optionalUser = userService.getUserByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            UserDto userDto = new UserDto(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getGender(),
                    user.getAge(),
                    user.getRole() != null ? user.getRole().name() : null
            );
            // Enrich with extended user fields
            userDto.setFirstName(user.getFirstName());
            userDto.setLastName(user.getLastName());
            userDto.setPhoneNumber(user.getPhoneNumber());
            userDto.setProfilePhoto(user.getProfilePhoto());
            userDto.setAccountStatus(user.getAccountStatus() != null ? user.getAccountStatus().name() : null);
            return ResponseEntity.ok(ApiResponse.success(userDto));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("User not found"));
    }

    /**
     * Handles user logout.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logoutUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // Blacklist the token in Redis for 1 hour (3,600,000 milliseconds)
            redisService.blacklistToken(token, 3600000L);
        }
        
        actionLogService.logAction(null, "LOGOUT", "User logged out");
        log.info("User logged out successfully");
        
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<String>> requestPasswordReset(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        try {
            String token = userService.generatePasswordResetToken(email);
            return ResponseEntity.ok(ApiResponse.success("Password reset link generated. Token: " + token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/password-reset/reset")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String password = body.get("password");
        try {
            userService.resetPassword(token, password);
            return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/email-verification/request")
    public ResponseEntity<ApiResponse<String>> requestEmailVerification(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        try {
            String token = userService.generateEmailVerification(email);
            return ResponseEntity.ok(ApiResponse.success("Email verification link generated. Token: " + token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/email-verification/verify")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        try {
            userService.verifyEmail(token);
            return ResponseEntity.ok(ApiResponse.success("Email address verified successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }
}
