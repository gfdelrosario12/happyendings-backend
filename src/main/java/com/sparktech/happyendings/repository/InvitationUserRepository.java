package com.sparktech.happyendings.repository;

import com.sparktech.happyendings.model.InvitationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitationUserRepository extends JpaRepository<InvitationUser, Long> {
}