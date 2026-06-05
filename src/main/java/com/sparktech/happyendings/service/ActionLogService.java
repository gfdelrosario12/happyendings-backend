package com.sparktech.happyendings.service;

import com.sparktech.happyendings.model.ActionLog;
import com.sparktech.happyendings.repository.ActionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActionLogService {

    @Autowired
    private ActionLogRepository actionLogRepository;

    public void logAction(Long userId, String actionType, String details) {
        ActionLog log = new ActionLog(userId, actionType, details);
        actionLogRepository.save(log);
    }
}