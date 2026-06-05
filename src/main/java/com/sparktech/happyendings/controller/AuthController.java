package com.sparktech.happyendings.controller;

import com.sparktech.happyendings.dto.AuthResponse;
import com.sparktech.happyendings.dto.LoginRequest;
import com.sparktech.happyendings.service.AuthService;
import com.sparktech.happyendings.service.ActionLogService; // Using the existing ActionLogService
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private ActionLogService actionLogService;

    /**
     * Authenticates a user and returns a JWT upon success.
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            log.info("Attempting login for user email: {}", loginRequest.getEmail());
            
            String jwt = authService.authenticateUser(loginRequest);
            
            // Log successful login
            actionLogService.logAction(null, "LOGIN_SUCCESS", "User logged in successfully with email: " + loginRequest.getEmail());
            log.info("Login successful for user email: {}", loginRequest.getEmail());
            
            return ResponseEntity.ok(new AuthResponse(jwt));
        } catch (AuthenticationException ex) {
            // Log failed login attempt
            actionLogService.logAction(null, "LOGIN_FAILED", "Failed login attempt for email: " + loginRequest.getEmail());
            log.warn("Login failed for user email: {}. Reason: {}", loginRequest.getEmail(), ex.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    /**
     * Handles user logout.
     * In a stateless JWT architecture, actual token invalidation often happens client-side 
     * (by removing the token). Server-side invalidation requires a token blacklist.
     * For this implementation, we log the action and return success.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        // Here you would typically extract the user info from the SecurityContext
        // For demonstration, we'll log a generic logout action.
        // In a real scenario, you'd extract the user ID/email from the request or security context.
        
        String authHeader = request.getHeader("Authorization");
        
        // Example logic for a token blacklist (not fully implemented here to keep it concise)
        // if (authHeader != null && authHeader.startsWith("Bearer ")) {
        //     String token = authHeader.substring(7);
        //     tokenBlacklistService.blacklistToken(token);
        // }
        
        actionLogService.logAction(null, "LOGOUT", "User logged out");
        log.info("User logged out successfully");
        
        return ResponseEntity.ok("Logged out successfully");
    }
}
