package com.sparktech.happyendings.controller;

import com.sparktech.happyendings.dto.ApiResponse;
import com.sparktech.happyendings.model.Guest;
import com.sparktech.happyendings.model.User;
import com.sparktech.happyendings.model.enums.InvitationRole;
import com.sparktech.happyendings.security.RequiresInvitationRole;
import com.sparktech.happyendings.service.GuestService;
import com.sparktech.happyendings.service.RSVPService;
import com.sparktech.happyendings.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/guests")
public class GuestController {

    @Autowired
    private GuestService guestService;

    @Autowired
    private RSVPService rsvpService;

    @Autowired
    private UserService userService;

    @PostMapping("/{invitationId}")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR})
    public ResponseEntity<ApiResponse<Guest>> addGuest(@PathVariable Long invitationId, @RequestBody Guest guest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        Guest created = guestService.addGuest(invitationId, guest, user.getId());
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PostMapping("/{invitationId}/import")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR})
    public ResponseEntity<ApiResponse<List<Guest>>> importGuests(@PathVariable Long invitationId, @RequestBody Map<String, String> body) {
        String csvData = body.get("csvData");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        List<Guest> imported = guestService.importGuests(invitationId, csvData, user.getId());
        return ResponseEntity.ok(ApiResponse.success(imported));
    }

    @PostMapping("/{invitationId}/merge")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR})
    public ResponseEntity<ApiResponse<Guest>> mergeGuests(
            @PathVariable Long invitationId, 
            @RequestBody Map<String, Long> body) {
        
        Long targetId = body.get("targetGuestId");
        Long duplicateId = body.get("duplicateGuestId");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Guest merged = guestService.mergeGuests(invitationId, targetId, duplicateId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(merged));
    }

    @GetMapping("/{invitationId}")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR})
    public ResponseEntity<ApiResponse<List<Guest>>> getGuests(@PathVariable Long invitationId) {
        List<Guest> list = guestService.getGuestsByInvitation(invitationId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/{invitationId}/{guestId}/magic-link")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR})
    public ResponseEntity<ApiResponse<String>> generateMagicLink(
            @PathVariable Long invitationId, 
            @PathVariable Long guestId) {
        
        String magicLink = rsvpService.generateMagicLink(invitationId, guestId);
        return ResponseEntity.ok(ApiResponse.success(magicLink));
    }
}
