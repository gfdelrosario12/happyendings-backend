package com.sparktech.happyendings.repository;

import com.sparktech.happyendings.model.InvitationViewMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvitationViewMetricRepository extends JpaRepository<InvitationViewMetric, Long> {
    List<InvitationViewMetric> findByInvitationId(Long invitationId);

    @Query("SELECT COUNT(v) FROM InvitationViewMetric v WHERE v.invitationId = :invitationId")
    long countViewsByInvitationId(@Param("invitationId") Long invitationId);
}
