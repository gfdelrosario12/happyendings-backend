package com.sparktech.happyendings.repository;

import com.sparktech.happyendings.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {

    @Query("SELECT g FROM Invitation i JOIN i.guests g WHERE i.id = :invitationId")
    List<Guest> findByInvitationId(@Param("invitationId") Long invitationId);

    @Query("SELECT g FROM Invitation i JOIN i.guests g WHERE i.id = :invitationId AND g.email = :email")
    Optional<Guest> findByInvitationIdAndEmail(@Param("invitationId") Long invitationId, @Param("email") String email);
}