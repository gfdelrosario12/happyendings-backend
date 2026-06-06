package com.sparktech.happyendings.service;

import com.sparktech.happyendings.dto.RegisterRequest;
import com.sparktech.happyendings.model.User;
import com.sparktech.happyendings.model.enums.Role;
import com.sparktech.happyendings.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActionLogService actionLogService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email address already in use.");
        }

        User user = new User();
        user.setName(registerRequest.getName());
        user.setGender(registerRequest.getGender());
        user.setEmail(registerRequest.getEmail());
        user.setAge(registerRequest.getAge());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.REGISTERED_USER); // Default role

        User newUser = userRepository.save(user);
        actionLogService.logAction(newUser.getId(), "USER_CREATED", "New user registered with email: " + user.getEmail());
        return newUser;
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
