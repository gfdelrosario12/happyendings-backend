package com.sparktech.happyendings.repository;

import com.sparktech.happyendings.model.GuestRoleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GuestRoleGroupRepository extends JpaRepository<GuestRoleGroup, Long> {
    List<GuestRoleGroup> findByInvitationIdOrderByOrderIndexAsc(Long invitationId);
}
