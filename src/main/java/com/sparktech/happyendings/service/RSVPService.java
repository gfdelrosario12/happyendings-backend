package com.sparktech.happyendings.service;

import com.sparktech.happyendings.dto.RsvpRequest;
import com.sparktech.happyendings.model.Guest;
import com.sparktech.happyendings.model.Invitation;
import com.sparktech.happyendings.model.enums.RsvpStatus;
import com.sparktech.happyendings.repository.GuestRepository;
import com.sparktech.happyendings.repository.InvitationRepository;
import com.sparktech.happyendings.security.JwtProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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

    @Autowired
    private JwtProvider jwtProvider;

    @Transactional
    public Guest updateRsvp(Long invitationId, RsvpRequest request) {
        log.info("Updating RSVP for guest ID: {} under invitation ID: {}", request.getGuestId(), invitationId);

        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));

        // Check RSVP expiration rules (e.g. if wedding date has passed, or custom deadline)
        if (invitation.getWeddingDate() != null && invitation.getWeddingDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("RSVP period has expired for this wedding invitation.");
        }

        // Fetch guest
        Guest guest = guestRepository.findById(request.getGuestId())
                .orElseThrow(() -> new IllegalArgumentException("Guest not found with ID: " + request.getGuestId()));

        // Validate plus-one rules
        if (request.getStatus() == RsvpStatus.ACCEPTED) {
            if (request.getAttendanceCount() > 1 && !guest.isPlusOneAllowed()) {
                throw new IllegalArgumentException("Plus-one attendance is not allowed for this guest.");
            }
            guest.setAttendanceCount(request.getAttendanceCount() > 0 ? request.getAttendanceCount() : 1);
        } else {
            guest.setAttendanceCount(0);
        }

        // Update details
        guest.setRsvpStatus(request.getStatus());
        if (request.getAdditionalNotes() != null) {
            guest.setAdditionalNotes(request.getAdditionalNotes());
        }
        if (request.getPlusOneName() != null) {
            guest.setPlusOneName(request.getPlusOneName());
        }
        if (request.getDietaryRestrictions() != null) {
            guest.setDietaryRestrictions(request.getDietaryRestrictions());
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
        kafkaProducerService.sendEvent("rsvp-events", "GuestRSVPSubmitted:" + updatedGuest.getId());
        kafkaProducerService.sendEvent("analytics-events", "RSVP_UPDATED_INVITATION_" + invitationId);

        return updatedGuest;
    }

    public String generateMagicLink(Long invitationId, Long guestId) {
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Guest not found."));

        // Generate stateless secure token
        String token = jwtProvider.generateTokenForSubject("guest:" + guest.getEmail());

        // Also cache it in Redis for rapid verification (valid for 30 days)
        redisService.set("guest:token:" + guestId, token, 2592000);

        String magicLink = "http://localhost:3000/rsvp/" + invitationId + "/" + guestId + "?token=" + token;

        // Dispatch email notification event
        kafkaProducerService.sendEvent("notification-events", "GuestInvited:" + guest.getEmail() + ":" + magicLink);

        return magicLink;
    }

    public String authenticateGuestToken(String token) {
        if (!jwtProvider.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired guest token.");
        }

        String subject = jwtProvider.getEmailFromToken(token);
        if (!subject.startsWith("guest:")) {
            throw new IllegalArgumentException("Token is not a valid guest token.");
        }

        return token; // Token is already a valid guest JWT!
    }
}
