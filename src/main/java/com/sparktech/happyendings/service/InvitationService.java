package com.sparktech.happyendings.service;

import com.sparktech.happyendings.model.Invitation;
import com.sparktech.happyendings.model.User;
import com.sparktech.happyendings.model.InvitationUser;
import com.sparktech.happyendings.model.enums.InvitationRole;
import com.sparktech.happyendings.repository.InvitationRepository;
import com.sparktech.happyendings.repository.InvitationUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvitationService {

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private InvitationUserRepository invitationUserRepository;

    @Autowired
    private ActionLogService actionLogService;

    @Transactional
    public Invitation createInvitation(Invitation invitation, User creator) {
        Invitation savedInvitation = invitationRepository.save(invitation);
        
        InvitationUser invitationUser = new InvitationUser();
        invitationUser.setInvitation(savedInvitation);
        invitationUser.setUser(creator);
        invitationUser.setRole(InvitationRole.COUPLE);
        
        invitationUserRepository.save(invitationUser);

        actionLogService.logAction(creator.getId(), "INVITATION_CREATED", "Invitation created with ID: " + savedInvitation.getId());
        return savedInvitation;
    }
}