package com.sparktech.happyendings.controller;

import com.sparktech.happyendings.dto.ApiResponse;
import com.sparktech.happyendings.model.*;
import com.sparktech.happyendings.model.enums.InvitationRole;
import com.sparktech.happyendings.security.RequiresInvitationRole;
import com.sparktech.happyendings.service.InvitationService;
import com.sparktech.happyendings.service.UserService;
import com.sparktech.happyendings.repository.InvitationViewMetricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private UserService userService;

    @Autowired
    private InvitationViewMetricRepository viewMetricRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<Invitation>> createInvitation(@RequestBody Invitation invitation) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
        Invitation created = invitationService.createInvitation(invitation, user);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @GetMapping("/{id}")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR, InvitationRole.GUEST})
    public ResponseEntity<ApiResponse<Invitation>> getInvitationById(@PathVariable Long id) {
        Invitation invitation = invitationService.getInvitationById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));
        return ResponseEntity.ok(ApiResponse.success(invitation));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<Invitation>> getInvitationBySlug(@PathVariable String slug) {
        Invitation invitation = invitationService.getInvitationBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));
        return ResponseEntity.ok(ApiResponse.success(invitation));
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<Invitation>>> getUserInvitations() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        List<Invitation> list = invitationService.listUserInvitations(user.getId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/{id}/publish")
    @RequiresInvitationRole({InvitationRole.COUPLE})
    public ResponseEntity<ApiResponse<Invitation>> publishInvitation(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        Invitation published = invitationService.publishInvitation(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(published));
    }

    @PostMapping("/{id}/archive")
    @RequiresInvitationRole({InvitationRole.COUPLE})
    public ResponseEntity<ApiResponse<Invitation>> archiveInvitation(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        Invitation archived = invitationService.archiveInvitation(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(archived));
    }

    @DeleteMapping("/{id}")
    @RequiresInvitationRole({InvitationRole.COUPLE})
    public ResponseEntity<ApiResponse<String>> softDeleteInvitation(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        invitationService.softDeleteInvitation(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Invitation soft deleted successfully."));
    }

    @PostMapping("/{id}/restore")
    @RequiresInvitationRole({InvitationRole.COUPLE})
    public ResponseEntity<ApiResponse<String>> restoreInvitation(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        invitationService.restoreInvitation(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Invitation restored successfully."));
    }

    @PutMapping("/{id}/details")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR})
    public ResponseEntity<ApiResponse<Invitation>> updateEventDetails(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> body) {
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // Map request payload to CeremonyDetails and ReceptionDetails
        Map<String, Object> ceremonyMap = (Map<String, Object>) body.get("ceremonyDetails");
        CeremonyDetails ceremony = new CeremonyDetails();
        if (ceremonyMap != null) {
            ceremony.setVenueName((String) ceremonyMap.get("venueName"));
            ceremony.setAddress((String) ceremonyMap.get("address"));
            ceremony.setOfficiant((String) ceremonyMap.get("officiant"));
        }

        Map<String, Object> receptionMap = (Map<String, Object>) body.get("receptionDetails");
        ReceptionDetails reception = new ReceptionDetails();
        if (receptionMap != null) {
            reception.setVenueName((String) receptionMap.get("venueName"));
            reception.setAddress((String) receptionMap.get("address"));
            reception.setAdditionalInstructions((String) receptionMap.get("additionalInstructions"));
        }

        Invitation updated = invitationService.updateCeremonyAndReception(id, ceremony, reception, user.getId());
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    // Program Segments
    @PostMapping("/{id}/program")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR})
    public ResponseEntity<ApiResponse<ProgramSegment>> addProgramSegment(@PathVariable Long id, @RequestBody ProgramSegment segment) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        ProgramSegment created = invitationService.addProgramSegment(id, segment, user.getId());
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PutMapping("/{id}/program/{segmentId}")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR})
    public ResponseEntity<ApiResponse<ProgramSegment>> updateProgramSegment(
            @PathVariable Long id, @PathVariable Long segmentId, @RequestBody ProgramSegment segment) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        ProgramSegment updated = invitationService.updateProgramSegment(segmentId, segment, user.getId());
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}/program/{segmentId}")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR})
    public ResponseEntity<ApiResponse<String>> deleteProgramSegment(@PathVariable Long id, @PathVariable Long segmentId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        invitationService.deleteProgramSegment(segmentId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Program segment deleted."));
    }

    @PostMapping("/{id}/program/reorder")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR})
    public ResponseEntity<ApiResponse<String>> reorderProgram(@PathVariable Long id, @RequestBody List<Long> segmentIds) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        invitationService.reorderProgramSegments(id, segmentIds, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Program reordered successfully."));
    }

    // Role Groups
    @PostMapping("/{id}/role-groups")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR})
    public ResponseEntity<ApiResponse<GuestRoleGroup>> addGuestRoleGroup(@PathVariable Long id, @RequestBody GuestRoleGroup group) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        GuestRoleGroup created = invitationService.addGuestRoleGroup(id, group, user.getId());
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PutMapping("/{id}/role-groups/{groupId}")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR})
    public ResponseEntity<ApiResponse<GuestRoleGroup>> updateGuestRoleGroup(
            @PathVariable Long id, @PathVariable Long groupId, @RequestBody GuestRoleGroup group) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        GuestRoleGroup updated = invitationService.updateGuestRoleGroup(groupId, group, user.getId());
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}/role-groups/{groupId}")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR})
    public ResponseEntity<ApiResponse<String>> deleteGuestRoleGroup(@PathVariable Long id, @PathVariable Long groupId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        invitationService.deleteGuestRoleGroup(groupId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Role group deleted."));
    }

    // Participant assignments
    @PostMapping("/{id}/program/{segmentId}/assign")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR})
    public ResponseEntity<ApiResponse<AssignedPerson>> assignParticipant(
            @PathVariable Long id, @PathVariable Long segmentId, @RequestBody AssignedPerson assignment) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        AssignedPerson created = invitationService.assignParticipantToSegment(segmentId, assignment, user.getId());
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @DeleteMapping("/{id}/program/{segmentId}/assign/{assignmentId}")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR})
    public ResponseEntity<ApiResponse<String>> removeAssignment(
            @PathVariable Long id, @PathVariable Long segmentId, @PathVariable Long assignmentId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        invitationService.removeParticipantFromSegment(segmentId, assignmentId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Assignment removed."));
    }

    @PostMapping("/{id}/coordinators")
    @RequiresInvitationRole({InvitationRole.COUPLE})
    public ResponseEntity<ApiResponse<String>> addCoordinator(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        invitationService.addCoordinator(id, body.get("email"), user.getId());
        return ResponseEntity.ok(ApiResponse.success("Coordinator added successfully."));
    }

    @GetMapping("/{id}/analytics")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalytics(@PathVariable Long id) {
        Invitation invitation = invitationService.getInvitationById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));

        long totalGuests = invitation.getGuests() != null ? invitation.getGuests().size() : 0;
        long totalRsvp = 0;
        long acceptedCount = 0;
        long declinedCount = 0;
        long maybeCount = 0;
        long totalAttendingPeople = 0;

        if (invitation.getGuests() != null) {
            for (Guest g : invitation.getGuests()) {
                if (g.getRsvpStatus() != com.sparktech.happyendings.model.enums.RsvpStatus.PENDING) {
                    totalRsvp++;
                }
                if (g.getRsvpStatus() == com.sparktech.happyendings.model.enums.RsvpStatus.ACCEPTED) {
                    acceptedCount++;
                    totalAttendingPeople += g.getAttendanceCount();
                } else if (g.getRsvpStatus() == com.sparktech.happyendings.model.enums.RsvpStatus.DECLINED) {
                    declinedCount++;
                } else if (g.getRsvpStatus() == com.sparktech.happyendings.model.enums.RsvpStatus.MAYBE) {
                    maybeCount++;
                }
            }
        }

        long pageViews = viewMetricRepository.countViewsByInvitationId(id);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalGuests", totalGuests);
        metrics.put("totalRsvp", totalRsvp);
        metrics.put("accepted", acceptedCount);
        metrics.put("declined", declinedCount);
        metrics.put("maybe", maybeCount);
        metrics.put("attendingPeople", totalAttendingPeople);
        metrics.put("pageViews", pageViews);
        metrics.put("rsvpConversionPercent", totalGuests > 0 ? (totalRsvp * 100.0 / totalGuests) : 0.0);

        return ResponseEntity.ok(ApiResponse.success(metrics));
    }
}
