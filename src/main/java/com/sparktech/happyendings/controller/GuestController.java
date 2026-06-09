package com.sparktech.happyendings.controller;

import com.sparktech.happyendings.model.Guest;
import com.sparktech.happyendings.service.GuestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations/{invitationId}/guests")
public class GuestController {

    @Autowired
    private GuestService guestService;

    @PostMapping
    public ResponseEntity<Guest> addGuest(
            @PathVariable Long invitationId,
            @RequestBody Guest guest,
            @RequestParam Long actorUserId) {
        Guest savedGuest = guestService.addGuest(invitationId, guest, actorUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedGuest);
    }

    @PostMapping("/import")
    public ResponseEntity<List<Guest>> importGuests(
            @PathVariable Long invitationId,
            @RequestBody String csvData,
            @RequestParam Long actorUserId) {
        List<Guest> importedGuests = guestService.importGuests(invitationId, csvData, actorUserId);
        return ResponseEntity.ok(importedGuests);
    }

    @PutMapping("/merge")
    public ResponseEntity<Guest> mergeGuests(
            @PathVariable Long invitationId,
            @RequestParam Long targetGuestId,
            @RequestParam Long duplicateGuestId,
            @RequestParam Long actorUserId) {
        Guest mergedGuest = guestService.mergeGuests(invitationId, targetGuestId, duplicateGuestId, actorUserId);
        return ResponseEntity.ok(mergedGuest);
    }

    @GetMapping
    public ResponseEntity<List<Guest>> getGuestsByInvitation(@PathVariable Long invitationId) {
        return ResponseEntity.ok(guestService.getGuestsByInvitation(invitationId));
    }
}
