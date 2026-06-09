package com.sparktech.happyendings.service;

import com.sparktech.happyendings.model.Guest;
import com.sparktech.happyendings.model.Invitation;
import com.sparktech.happyendings.model.enums.RsvpStatus;
import com.sparktech.happyendings.repository.GuestRepository;
import com.sparktech.happyendings.repository.InvitationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GuestService {

    private static final Logger log = LoggerFactory.getLogger(GuestService.class);

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private ActionLogService actionLogService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private RedisService redisService;

    @Transactional
    public Guest rsvp(Long guestId, RsvpStatus rsvpStatus) {
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid guest ID: " + guestId));

        guest.setRsvpStatus(rsvpStatus);
        Guest updatedGuest = guestRepository.save(guest);

        Long userId = guest.getLinkedUser() != null ? guest.getLinkedUser().getId() : null;
        actionLogService.logAction(userId, "GUEST_RSVP", "Guest with ID " + guestId + " RSVP'd with status " + rsvpStatus);

        return updatedGuest;
    }

    @Transactional
    public Guest addGuest(Long invitationId, Guest guest, Long actorUserId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));

        // Duplicate check
        Optional<Guest> existing = guestRepository.findByInvitationIdAndEmail(invitationId, guest.getEmail());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Guest with email " + guest.getEmail() + " is already invited.");
        }

        if (guest.getName() == null) {
            guest.setName((guest.getFirstName() != null ? guest.getFirstName() : "") + " " + (guest.getLastName() != null ? guest.getLastName() : ""));
        }
        guest.setRsvpStatus(RsvpStatus.PENDING);

        if (invitation.getGuests() == null) {
            invitation.setGuests(new ArrayList<>());
        }

        Guest savedGuest = guestRepository.save(guest);
        invitation.getGuests().add(savedGuest);
        invitationRepository.save(invitation);

        // Invalidate Redis Cache
        redisService.delete("invitation:guests:" + invitationId);

        actionLogService.logAction(actorUserId, "GUEST_ADDED", "Guest added: " + guest.getEmail() + " to invitation ID: " + invitationId);
        kafkaProducerService.sendEvent("guest-events", "GuestInvited:" + savedGuest.getId());

        return savedGuest;
    }

    @Transactional
    public List<Guest> importGuests(Long invitationId, String csvData, Long actorUserId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));

        List<Guest> importedGuests = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(csvData))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (isHeader) {
                    isHeader = false; // Skip CSV headers
                    continue;
                }

                // Simple comma splitting supporting quotes
                String[] cols = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if (cols.length < 3) continue; // Must have email, firstName, lastName

                String email = cols[0].replace("\"", "").trim();
                String firstName = cols[1].replace("\"", "").trim();
                String lastName = cols[2].replace("\"", "").trim();

                String phoneNumber = cols.length > 3 ? cols[3].replace("\"", "").trim() : "";
                boolean plusOneAllowed = cols.length > 4 && Boolean.parseBoolean(cols[4].replace("\"", "").trim());
                String plusOneName = cols.length > 5 ? cols[5].replace("\"", "").trim() : "";
                String dietaryRestrictions = cols.length > 6 ? cols[6].replace("\"", "").trim() : "";
                String notes = cols.length > 7 ? cols[7].replace("\"", "").trim() : "";

                // Duplicate bypass or override check
                Optional<Guest> existing = guestRepository.findByInvitationIdAndEmail(invitationId, email);
                if (existing.isPresent()) {
                    // Skip or log duplicates
                    log.info("Skip duplicate email in import: {}", email);
                    continue;
                }

                Guest guest = new Guest();
                guest.setEmail(email);
                guest.setFirstName(firstName);
                guest.setLastName(lastName);
                guest.setName(firstName + " " + lastName);
                guest.setPhoneNumber(phoneNumber);
                guest.setPlusOneAllowed(plusOneAllowed);
                guest.setPlusOneName(plusOneName);
                guest.setDietaryRestrictions(dietaryRestrictions);
                guest.setAdditionalNotes(notes);
                guest.setRsvpStatus(RsvpStatus.PENDING);

                Guest saved = guestRepository.save(guest);
                importedGuests.add(saved);
            }
        } catch (Exception e) {
            log.error("Failed to parse CSV data", e);
            throw new IllegalArgumentException("Invalid CSV structure: " + e.getMessage());
        }

        if (!importedGuests.isEmpty()) {
            if (invitation.getGuests() == null) {
                invitation.setGuests(new ArrayList<>());
            }
            invitation.getGuests().addAll(importedGuests);
            invitationRepository.save(invitation);

            redisService.delete("invitation:guests:" + invitationId);

            actionLogService.logAction(actorUserId, "GUESTS_IMPORTED", "Imported " + importedGuests.size() + " guests from CSV.");
            for (Guest g : importedGuests) {
                kafkaProducerService.sendEvent("guest-events", "GuestInvited:" + g.getId());
            }
        }

        return importedGuests;
    }

    @Transactional
    public Guest mergeGuests(Long invitationId, Long targetGuestId, Long duplicateGuestId, Long actorUserId) {
        Guest target = guestRepository.findById(targetGuestId)
                .orElseThrow(() -> new IllegalArgumentException("Target guest not found."));
        Guest duplicate = guestRepository.findById(duplicateGuestId)
                .orElseThrow(() -> new IllegalArgumentException("Duplicate guest not found."));

        // Merge attributes
        if (target.getPhoneNumber() == null || target.getPhoneNumber().isEmpty()) {
            target.setPhoneNumber(duplicate.getPhoneNumber());
        }
        if (target.getDietaryRestrictions() == null || target.getDietaryRestrictions().isEmpty()) {
            target.setDietaryRestrictions(duplicate.getDietaryRestrictions());
        }
        if (duplicate.getAdditionalNotes() != null && !duplicate.getAdditionalNotes().isEmpty()) {
            target.setAdditionalNotes((target.getAdditionalNotes() != null ? target.getAdditionalNotes() + "; " : "") + duplicate.getAdditionalNotes());
        }
        if (duplicate.isPlusOneAllowed()) {
            target.setPlusOneAllowed(true);
            if (target.getPlusOneName() == null || target.getPlusOneName().isEmpty()) {
                target.setPlusOneName(duplicate.getPlusOneName());
            }
        }

        // Delete duplicate
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));
        invitation.getGuests().remove(duplicate);
        invitationRepository.save(invitation);
        guestRepository.delete(duplicate);

        Guest savedTarget = guestRepository.save(target);

        redisService.delete("invitation:guests:" + invitationId);

        actionLogService.logAction(actorUserId, "GUESTS_MERGED", "Merged guest " + duplicate.getEmail() + " into " + target.getEmail());
        kafkaProducerService.sendEvent("guest-events", "GuestRSVPUpdated:" + savedTarget.getId());

        return savedTarget;
    }

    public List<Guest> getGuestsByInvitation(Long invitationId) {
        String cacheKey = "invitation:guests:" + invitationId;
        List<Guest> cached = redisService.get(cacheKey, List.class);
        if (cached != null) {
            return cached;
        }

        List<Guest> list = guestRepository.findByInvitationId(invitationId);
        redisService.set(cacheKey, list, 120); // 2 min cache
        return list;
    }
}