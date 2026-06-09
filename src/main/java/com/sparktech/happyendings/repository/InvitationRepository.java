package com.sparktech.happyendings.repository;

import com.sparktech.happyendings.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    @Query("SELECT i FROM Invitation i JOIN i.guests g WHERE g.id = :guestId")
    Optional<Invitation> findByGuestId(@Param("guestId") Long guestId);

    Optional<Invitation> findBySlugAndDeletedFalse(String slug);

    List<Invitation> findByDeletedFalse();

    @Query("SELECT i FROM Invitation i WHERE (i.partnerA.id = :userId OR i.partnerB.id = :userId) AND i.deleted = false")
    List<Invitation> findActiveByPartnerId(@Param("userId") Long userId);
}