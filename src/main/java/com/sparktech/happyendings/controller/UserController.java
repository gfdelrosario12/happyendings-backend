package com.sparktech.happyendings.controller;

import com.sparktech.happyendings.dto.ApiResponse;
import com.sparktech.happyendings.dto.UserDto;
import com.sparktech.happyendings.model.User;
import com.sparktech.happyendings.model.ActionLog;
import com.sparktech.happyendings.model.enums.AccountStatus;
import com.sparktech.happyendings.service.UserService;
import com.sparktech.happyendings.service.ActionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ActionLogService actionLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        String actorEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User actor = userService.getUserByEmail(actorEmail)
                .orElseThrow(() -> new IllegalArgumentException("Actor not found."));

        if (actor.getRole() == null || !actor.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Only administrators can retrieve the list of users."));
        }

        List<UserDto> list = userService.getAllUsers().stream().map(u -> {
            UserDto dto = new UserDto(
                    u.getId(),
                    u.getName(),
                    u.getEmail(),
                    u.getGender(),
                    u.getAge(),
                    u.getRole() != null ? u.getRole().name() : null
            );
            dto.setFirstName(u.getFirstName());
            dto.setLastName(u.getLastName());
            dto.setPhoneNumber(u.getPhoneNumber());
            dto.setProfilePhoto(u.getProfilePhoto());
            dto.setAccountStatus(u.getAccountStatus() != null ? u.getAccountStatus().name() : null);
            return dto;
        }).toList();

        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(@RequestBody Map<String, String> body) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        String firstName = body.get("firstName");
        String lastName = body.get("lastName");
        String phoneNumber = body.get("phoneNumber");
        String profilePhoto = body.get("profilePhoto");

        User updatedUser = userService.updateUserProfile(user.getId(), firstName, lastName, phoneNumber, profilePhoto);

        UserDto dto = new UserDto(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getGender(),
                updatedUser.getAge(),
                updatedUser.getRole() != null ? updatedUser.getRole().name() : null
        );
        dto.setFirstName(updatedUser.getFirstName());
        dto.setLastName(updatedUser.getLastName());
        dto.setPhoneNumber(updatedUser.getPhoneNumber());
        dto.setProfilePhoto(updatedUser.getProfilePhoto());
        dto.setAccountStatus(updatedUser.getAccountStatus() != null ? updatedUser.getAccountStatus().name() : null);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        AccountStatus status = AccountStatus.valueOf(statusStr.toUpperCase());
        
        // Log who did the action
        String actorEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User actor = userService.getUserByEmail(actorEmail)
                .orElseThrow(() -> new IllegalArgumentException("Actor not found."));

        if (!actor.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Only administrators can suspend or delete user accounts."));
        }

        userService.changeAccountStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("User status changed successfully to: " + status));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<ActionLog>>> getAuditLogs() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (!user.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied: Admin credentials required."));
        }

        List<ActionLog> logs = actionLogService.getAllLogs();
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
