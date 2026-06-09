package com.sparktech.happyendings.repository;

import com.sparktech.happyendings.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByInvitationId(Long invitationId);
    List<Reminder> findByStatusAndScheduledTimeBefore(String status, LocalDateTime time);
}
