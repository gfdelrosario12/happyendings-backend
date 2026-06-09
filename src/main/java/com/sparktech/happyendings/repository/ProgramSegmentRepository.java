package com.sparktech.happyendings.repository;

import com.sparktech.happyendings.model.ProgramSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProgramSegmentRepository extends JpaRepository<ProgramSegment, Long> {
    List<ProgramSegment> findByInvitationIdOrderByOrderIndexAsc(Long invitationId);
    List<ProgramSegment> findByInvitationIdAndParentIdOrderByOrderIndexAsc(Long invitationId, Long parentId);
}
