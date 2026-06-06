package com.sparktech.happyendings.controller;

import com.sparktech.happyendings.dto.AuthResponse;
import com.sparktech.happyendings.dto.LoginRequest;
import com.sparktech.happyendings.dto.RegisterRequest;
import com.sparktech.happyendings.dto.UserDto;
import com.sparktech.happyendings.model.User;
import com.sparktech.happyendings.service.AuthService;
import com.sparktech.happyendings.service.UserService;
import com.sparktech.happyendings.service.ActionLogService;
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
     * Registers a new user account.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
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
            return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed (bad request): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Registration failed due to server error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed");
        }
    }

    /**
     * Retrieves the currently authenticated user's details.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
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
            return ResponseEntity.ok(userDto);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    /**
     * Handles user logout.
     * In a stateless JWT architecture, actual token invalidation often happens client-side 
     * (by removing the token). Server-side invalidation requires a token blacklist.
     * For this implementation, we log the action and return success.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        actionLogService.logAction(null, "LOGOUT", "User logged out");
        log.info("User logged out successfully");
        
        return ResponseEntity.ok("Logged out successfully");
    }
}
