package com.sparktech.happyendings.controller;

import com.sparktech.happyendings.model.Guest;
import com.sparktech.happyendings.service.GuestService;
import com.sparktech.happyendings.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/invitations/{invitationId}/guests", "/api/guests/{invitationId}"})
public class GuestController {

    @Autowired
    private GuestService guestService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<Guest> addGuest(
            @PathVariable Long invitationId,
            @RequestBody Guest guest,
            @RequestParam(required = false) Long actorUserId) {
        Long actorId = actorUserId;
        if (actorId == null) {
            String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            actorId = userService.getUserByEmail(email)
                    .map(com.sparktech.happyendings.model.User::getId)
                    .orElse(null);
        }
        Guest savedGuest = guestService.addGuest(invitationId, guest, actorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedGuest);
    }

    @PostMapping("/import")
    public ResponseEntity<List<Guest>> importGuests(
            @PathVariable Long invitationId,
            @RequestBody String csvData,
            @RequestParam(required = false) Long actorUserId) {
        Long actorId = actorUserId;
        if (actorId == null) {
            String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            actorId = userService.getUserByEmail(email)
                    .map(com.sparktech.happyendings.model.User::getId)
                    .orElse(null);
        }
        List<Guest> importedGuests = guestService.importGuests(invitationId, csvData, actorId);
        return ResponseEntity.ok(importedGuests);
    }

    @PutMapping("/merge")
    public ResponseEntity<Guest> mergeGuests(
            @PathVariable Long invitationId,
            @RequestParam Long targetGuestId,
            @RequestParam Long duplicateGuestId,
            @RequestParam(required = false) Long actorUserId) {
        Long actorId = actorUserId;
        if (actorId == null) {
            String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            actorId = userService.getUserByEmail(email)
                    .map(com.sparktech.happyendings.model.User::getId)
                    .orElse(null);
        }
        Guest mergedGuest = guestService.mergeGuests(invitationId, targetGuestId, duplicateGuestId, actorId);
        return ResponseEntity.ok(mergedGuest);
    }

    @GetMapping
    public ResponseEntity<List<Guest>> getGuestsByInvitation(@PathVariable Long invitationId) {
        return ResponseEntity.ok(guestService.getGuestsByInvitation(invitationId));
    }
}
