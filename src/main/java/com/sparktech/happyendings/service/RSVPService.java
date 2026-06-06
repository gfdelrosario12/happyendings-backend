package com.sparktech.happyendings.service;

import com.sparktech.happyendings.dto.RsvpRequest;
import com.sparktech.happyendings.model.Guest;
import com.sparktech.happyendings.model.Invitation;
import com.sparktech.happyendings.repository.GuestRepository;
import com.sparktech.happyendings.repository.InvitationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RSVPService {

    private static final Logger log = LoggerFactory.getLogger(RSVPService.class);

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Transactional
    public Guest updateRsvp(Long invitationId, RsvpRequest request) {
        log.info("Updating RSVP for guest ID: {} under invitation ID: {}", request.getGuestId(), invitationId);

        // Fetch guest
        Guest guest = guestRepository.findById(request.getGuestId())
                .orElseThrow(() -> new IllegalArgumentException("Guest not found with ID: " + request.getGuestId()));

        // Update details
        guest.setRsvpStatus(request.getStatus());
        if (request.getAdditionalNotes() != null) {
            guest.setAdditionalNotes(request.getAdditionalNotes());
        }

        Guest updatedGuest = guestRepository.save(guest);

        // Cache Invalidation
        String detailsKey = "invitation:details:" + invitationId;
        String guestsKey = "invitation:guests:" + invitationId;
        String analyticsKey = "analytics:rsvp:" + invitationId;

        redisService.delete(detailsKey);
        redisService.delete(guestsKey);
        redisService.delete(analyticsKey);
        log.info("Invalidated Redis cache keys: {}, {}, and {}", detailsKey, guestsKey, analyticsKey);

        // Emit Kafka Events
        kafkaProducerService.sendEvent("guest.rsvp.updated", request);
        kafkaProducerService.sendEvent("analytics.event", "RSVP_UPDATED_INVITATION_" + invitationId);

        return updatedGuest;
    }
}
