package com.sparktech.happyendings.controller;

import com.sparktech.happyendings.dto.ApiResponse;
import com.sparktech.happyendings.model.Reminder;
import com.sparktech.happyendings.model.User;
import com.sparktech.happyendings.model.enums.InvitationRole;
import com.sparktech.happyendings.security.RequiresInvitationRole;
import com.sparktech.happyendings.service.ReminderService;
import com.sparktech.happyendings.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations/{invitationId}/reminders")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private UserService userService;

    @PostMapping("/schedule")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR})
    public ResponseEntity<ApiResponse<Reminder>> scheduleReminder(
            @PathVariable Long invitationId,
            @RequestBody Map<String, Object> body) {
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User actor = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Actor not found."));

        Long guestId = body.containsKey("guestId") && body.get("guestId") != null 
                ? ((Number) body.get("guestId")).longValue() : null;
        String type = (String) body.getOrDefault("type", "AUTOMATED");
        String scheduledTimeStr = (String) body.get("scheduledTime");
        LocalDateTime scheduledTime = scheduledTimeStr != null 
                ? LocalDateTime.parse(scheduledTimeStr) : LocalDateTime.now();
        String template = (String) body.getOrDefault("template", "Dear guest, please RSVP to our wedding!");

        Reminder reminder = reminderService.scheduleReminder(invitationId, guestId, type, scheduledTime, template, actor.getId());
        return ResponseEntity.ok(ApiResponse.success(reminder));
    }

    @PostMapping("/{reminderId}/execute")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR})
    public ResponseEntity<ApiResponse<String>> executeReminder(
            @PathVariable Long invitationId,
            @PathVariable Long reminderId) {
        
        reminderService.executeReminder(reminderId);
        return ResponseEntity.ok(ApiResponse.success("Reminder execution triggered successfully."));
    }
}
