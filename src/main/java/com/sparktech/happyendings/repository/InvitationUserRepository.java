package com.sparktech.happyendings.repository;

import com.sparktech.happyendings.model.InvitationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InvitationUserRepository extends JpaRepository<InvitationUser, Long> {
    Optional<InvitationUser> findByInvitationIdAndUserId(Long invitationId, Long userId);
}