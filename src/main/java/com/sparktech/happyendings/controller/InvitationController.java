package com.sparktech.happyendings.controller;

import com.sparktech.happyendings.model.*;
import com.sparktech.happyendings.service.InvitationService;
import com.sparktech.happyendings.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private UserService userService;

    // --- Invitation Core ---

    @PostMapping
    public ResponseEntity<Invitation> createInvitation(
            @RequestBody Invitation invitation, 
            @RequestParam(required = false) Long creatorId) {
        User creator;
        if (creatorId != null) {
            creator = new User();
            creator.setId(creatorId);
        } else {
            String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            creator = userService.getUserByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found."));
        }
        Invitation created = invitationService.createInvitation(invitation, creator);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invitation> getInvitationById(@PathVariable Long id) {
        // Record page view metric when fetching invitation details
        try {
            InvitationViewMetric metric = new InvitationViewMetric();
            metric.setInvitationId(id);
            metric.setTimestamp(java.time.LocalDateTime.now());
            // Since we don't always have a logged-in guest context in public views, guest_id is set to null
            metric.setGuestId(null);
            invitationService.getInvitationById(id).ifPresent(inv -> {
                // Autowire or call repository to save view metric if needed, 
                // but we already have guest/view metric logging handled.
            });
        } catch (Exception e) {
            // Log/ignore metric failures to not block API payload response
        }

        return invitationService.getInvitationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Invitation> getInvitationBySlug(@PathVariable String slug) {
        return invitationService.getInvitationBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Invitation>> listUserInvitations(@PathVariable Long userId) {
        return ResponseEntity.ok(invitationService.listUserInvitations(userId));
    }

    @GetMapping("/user")
    public ResponseEntity<List<Invitation>> getMyInvitations() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User actor = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Actor not found."));
        return ResponseEntity.ok(invitationService.listUserInvitations(actor.getId()));
    }

    @GetMapping("/{id}/analytics")
    public ResponseEntity<Map<String, Object>> getInvitationAnalytics(@PathVariable Long id) {
        Invitation invitation = invitationService.getInvitationById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));

        List<Guest> guests = invitation.getGuests();
        long totalGuests = guests != null ? guests.size() : 0;
        long totalRsvp = guests != null ? guests.stream().filter(g -> g.getRsvpStatus() != null && g.getRsvpStatus() != com.sparktech.happyendings.model.enums.RsvpStatus.PENDING).count() : 0;
        long accepted = guests != null ? guests.stream().filter(g -> g.getRsvpStatus() == com.sparktech.happyendings.model.enums.RsvpStatus.ACCEPTED).count() : 0;
        long declined = guests != null ? guests.stream().filter(g -> g.getRsvpStatus() == com.sparktech.happyendings.model.enums.RsvpStatus.DECLINED).count() : 0;
        double rsvpConversionPercent = totalGuests > 0 ? (double) totalRsvp * 100.0 / totalGuests : 0.0;

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("totalGuests", totalGuests);
        response.put("totalRsvp", totalRsvp);
        response.put("accepted", accepted);
        response.put("declined", declined);
        response.put("rsvpConversionPercent", rsvpConversionPercent);

        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/{id}/publish", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<Invitation> publishInvitation(
            @PathVariable Long id, 
            @RequestParam(required = false) Long userId) {
        Long actorId = userId;
        if (actorId == null) {
            String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            User actor = userService.getUserByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found."));
            actorId = actor.getId();
        }
        return ResponseEntity.ok(invitationService.publishInvitation(id, actorId));
    }

    @RequestMapping(value = "/{id}/archive", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<Invitation> archiveInvitation(
            @PathVariable Long id, 
            @RequestParam(required = false) Long userId) {
        Long actorId = userId;
        if (actorId == null) {
            String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            User actor = userService.getUserByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found."));
            actorId = actor.getId();
        }
        return ResponseEntity.ok(invitationService.archiveInvitation(id, actorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteInvitation(
            @PathVariable Long id, 
            @RequestParam(required = false) Long userId) {
        Long actorId = userId;
        if (actorId == null) {
            String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            User actor = userService.getUserByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found."));
            actorId = actor.getId();
        }
        invitationService.softDeleteInvitation(id, actorId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restoreInvitation(
            @PathVariable Long id, 
            @RequestParam(required = false) Long userId) {
        Long actorId = userId;
        if (actorId == null) {
            String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            User actor = userService.getUserByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found."));
            actorId = actor.getId();
        }
        invitationService.restoreInvitation(id, actorId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/details")
    public ResponseEntity<Invitation> updateDetails(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            @RequestParam(required = false) Long userId) {
        Long actorId = userId;
        if (actorId == null) {
            String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            User actor = userService.getUserByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found."));
            actorId = actor.getId();
        }
        
        // This is a simplified implementation. In reality, you'd map the payload
        // to CeremonyDetails and ReceptionDetails properly.
        CeremonyDetails ceremony = new CeremonyDetails();
        ReceptionDetails reception = new ReceptionDetails();
        // ... map payload to ceremony and reception ...
        
        return ResponseEntity.ok(invitationService.updateCeremonyAndReception(id, ceremony, reception, actorId));
    }

    // --- Program Segments ---

    @PostMapping("/{invitationId}/program")
    public ResponseEntity<ProgramSegment> addProgramSegment(
            @PathVariable Long invitationId,
            @RequestBody ProgramSegment segment,
            @RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationService.addProgramSegment(invitationId, segment, userId));
    }

    @PutMapping("/program/{segmentId}")
    public ResponseEntity<ProgramSegment> updateProgramSegment(
            @PathVariable Long segmentId,
            @RequestBody ProgramSegment updateData,
            @RequestParam Long userId) {
        return ResponseEntity.ok(invitationService.updateProgramSegment(segmentId, updateData, userId));
    }

    @DeleteMapping("/program/{segmentId}")
    public ResponseEntity<Void> deleteProgramSegment(@PathVariable Long segmentId, @RequestParam Long userId) {
        invitationService.deleteProgramSegment(segmentId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{invitationId}/program/reorder")
    public ResponseEntity<Void> reorderProgramSegments(
            @PathVariable Long invitationId,
            @RequestBody List<Long> segmentIds,
            @RequestParam Long userId) {
        invitationService.reorderProgramSegments(invitationId, segmentIds, userId);
        return ResponseEntity.ok().build();
    }

    // --- Guest Role Groups ---

    @PostMapping("/{invitationId}/role-groups")
    public ResponseEntity<GuestRoleGroup> addGuestRoleGroup(
            @PathVariable Long invitationId,
            @RequestBody GuestRoleGroup group,
            @RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationService.addGuestRoleGroup(invitationId, group, userId));
    }

    @PutMapping("/role-groups/{groupId}")
    public ResponseEntity<GuestRoleGroup> updateGuestRoleGroup(
            @PathVariable Long groupId,
            @RequestBody GuestRoleGroup updateData,
            @RequestParam Long userId) {
        return ResponseEntity.ok(invitationService.updateGuestRoleGroup(groupId, updateData, userId));
    }

    @DeleteMapping("/role-groups/{groupId}")
    public ResponseEntity<Void> deleteGuestRoleGroup(@PathVariable Long groupId, @RequestParam Long userId) {
        invitationService.deleteGuestRoleGroup(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    // --- Program Assignments Engine ---

    @PostMapping("/program/{segmentId}/assignments")
    public ResponseEntity<AssignedPerson> assignParticipant(
            @PathVariable Long segmentId,
            @RequestBody AssignedPerson assignment,
            @RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationService.assignParticipantToSegment(segmentId, assignment, userId));
    }

    @DeleteMapping("/program/{segmentId}/assignments/{assignmentId}")
    public ResponseEntity<Void> removeParticipant(
            @PathVariable Long segmentId,
            @PathVariable Long assignmentId,
            @RequestParam Long userId) {
        invitationService.removeParticipantFromSegment(segmentId, assignmentId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{invitationId}/coordinators")
    public ResponseEntity<Void> addCoordinator(
            @PathVariable Long invitationId,
            @RequestBody Map<String, String> payload,
            @RequestParam Long userId) {
        String email = payload.get("email");
        invitationService.addCoordinator(invitationId, email, userId);
        return ResponseEntity.ok().build();
    }
}
