package com.sparktech.happyendings.service;

import com.sparktech.happyendings.model.Guest;
import com.sparktech.happyendings.model.Reminder;
import com.sparktech.happyendings.repository.GuestRepository;
import com.sparktech.happyendings.repository.ReminderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private ActionLogService actionLogService;

    @Transactional
    public Reminder scheduleReminder(Long invitationId, Long guestId, String type, LocalDateTime scheduledTime, String template, Long actorUserId) {
        Reminder reminder = new Reminder();
        reminder.setInvitationId(invitationId);
        reminder.setGuestId(guestId);
        reminder.setType(type); // SCHEDULED, AUTOMATED, MANUAL
        reminder.setScheduledTime(scheduledTime);
        reminder.setMessageTemplate(template);
        reminder.setStatus("PENDING");

        Reminder saved = reminderRepository.save(reminder);
        actionLogService.logAction(actorUserId, "REMINDER_SCHEDULED", "Scheduled a " + type + " reminder for " + (guestId != null ? "guest ID: " + guestId : "all guests"));

        // Produce scheduling task event
        kafkaProducerService.sendEvent("notification-events", "ReminderScheduled:" + saved.getId());

        return saved;
    }

    @Transactional
    public void executeReminder(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("Reminder task not found."));

        if (!reminder.getStatus().equals("PENDING")) {
            log.warn("Reminder {} is not pending, state is: {}", reminderId, reminder.getStatus());
            return;
        }

        try {
            if (reminder.getGuestId() != null) {
                // Send to single guest
                Guest guest = guestRepository.findById(reminder.getGuestId())
                        .orElseThrow(() -> new IllegalArgumentException("Guest not found."));
                sendEmail(guest.getEmail(), reminder.getMessageTemplate());
            } else {
                // Send to all guests of the invitation
                List<Guest> guests = guestRepository.findByInvitationId(reminder.getInvitationId());
                for (Guest guest : guests) {
                    sendEmail(guest.getEmail(), reminder.getMessageTemplate());
                }
            }

            reminder.setStatus("SENT");
            reminder.setSentTime(LocalDateTime.now());
            reminderRepository.save(reminder);
            actionLogService.logAction(null, "REMINDER_SENT", "Successfully sent reminder ID: " + reminderId);
        } catch (Exception e) {
            log.error("Failed to execute reminder: {}", reminderId, e);
            reminder.setStatus("FAILED");
            reminderRepository.save(reminder);
            actionLogService.logAction(null, "REMINDER_FAILED", "Failed to send reminder ID: " + reminderId + " - Error: " + e.getMessage());
        }
    }

    private void sendEmail(String toEmail, String content) {
        // Queue sending task to Kafka
        kafkaProducerService.sendEvent("notification-events", "SendEmail:" + toEmail + ":" + content);
        log.info("Reminder email queued to: {}", toEmail);
    }
}
