package com.sparktech.happyendings.controller;

import com.sparktech.happyendings.model.*;
import com.sparktech.happyendings.service.InvitationService;
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

    // --- Invitation Core ---

    @PostMapping
    public ResponseEntity<Invitation> createInvitation(@RequestBody Invitation invitation, @RequestParam Long creatorId) {
        // In a real app, creatorId would come from the SecurityContext
        User creator = new User();
        creator.setId(creatorId);
        Invitation created = invitationService.createInvitation(invitation, creator);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invitation> getInvitationById(@PathVariable Long id) {
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

    @PutMapping("/{id}/publish")
    public ResponseEntity<Invitation> publishInvitation(@PathVariable Long id, @RequestParam Long userId) {
        return ResponseEntity.ok(invitationService.publishInvitation(id, userId));
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Invitation> archiveInvitation(@PathVariable Long id, @RequestParam Long userId) {
        return ResponseEntity.ok(invitationService.archiveInvitation(id, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteInvitation(@PathVariable Long id, @RequestParam Long userId) {
        invitationService.softDeleteInvitation(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restoreInvitation(@PathVariable Long id, @RequestParam Long userId) {
        invitationService.restoreInvitation(id, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/details")
    public ResponseEntity<Invitation> updateDetails(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            @RequestParam Long userId) {
        
        // This is a simplified implementation. In reality, you'd map the payload
        // to CeremonyDetails and ReceptionDetails properly.
        CeremonyDetails ceremony = new CeremonyDetails();
        ReceptionDetails reception = new ReceptionDetails();
        // ... map payload to ceremony and reception ...
        
        return ResponseEntity.ok(invitationService.updateCeremonyAndReception(id, ceremony, reception, userId));
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
