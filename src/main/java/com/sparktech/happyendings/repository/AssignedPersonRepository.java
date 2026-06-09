package com.sparktech.happyendings.repository;

import com.sparktech.happyendings.model.AssignedPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssignedPersonRepository extends JpaRepository<AssignedPerson, Long> {
    List<AssignedPerson> findByProgramSegmentId(Long programSegmentId);
    List<AssignedPerson> findByParticipantIdAndParticipantType(Long participantId, String participantType);
}
