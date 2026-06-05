package com.sparktech.happyendings.service;

import com.sparktech.happyendings.model.Guest;
import com.sparktech.happyendings.model.enums.RsvpStatus;
import com.sparktech.happyendings.repository.GuestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GuestService {

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private ActionLogService actionLogService;

    public Guest rsvp(Long guestId, RsvpStatus rsvpStatus) {
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid guest ID: " + guestId));
        
        guest.setRsvpStatus(rsvpStatus);
        Guest updatedGuest = guestRepository.save(guest);
        
        Long userId = guest.getLinkedUser() != null ? guest.getLinkedUser().getId() : null;
        actionLogService.logAction(userId, "GUEST_RSVP", "Guest with ID " + guestId + " RSVP'd with status " + rsvpStatus);
        
        return updatedGuest;
    }
}