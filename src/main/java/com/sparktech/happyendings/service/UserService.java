package com.sparktech.happyendings.service;

import com.sparktech.happyendings.model.User;
import com.sparktech.happyendings.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActionLogService actionLogService;

    public User createUser(User user) {
        User newUser = userRepository.save(user);
        actionLogService.logAction(newUser.getId(), "USER_CREATED", "New user registered with email: " + user.getEmail());
        return newUser;
    }
}