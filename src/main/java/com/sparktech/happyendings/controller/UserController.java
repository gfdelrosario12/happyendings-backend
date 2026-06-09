package com.sparktech.happyendings.controller;

import com.sparktech.happyendings.dto.UserDto;
import com.sparktech.happyendings.model.User;
import com.sparktech.happyendings.model.enums.AccountStatus;
import com.sparktech.happyendings.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(convertToDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserDto> getUserByEmail(@RequestParam String email) {
        return userService.getUserByEmail(email)
                .map(user -> ResponseEntity.ok(convertToDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<UserDto> updateUserProfile(@PathVariable Long id, @RequestBody Map<String, String> profileData) {
        User updatedUser = userService.updateUserProfile(
                id,
                profileData.get("firstName"),
                profileData.get("lastName"),
                profileData.get("phoneNumber"),
                profileData.get("profilePhoto")
        );
        return ResponseEntity.ok(convertToDto(updatedUser));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> changeAccountStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        AccountStatus status = AccountStatus.valueOf(statusUpdate.get("status"));
        userService.changeAccountStatus(id, status);
        return ResponseEntity.ok().build();
    }

    private UserDto convertToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getGender(),
                user.getAge(),
                user.getRole() != null ? user.getRole().name() : null
        );
    }
}
