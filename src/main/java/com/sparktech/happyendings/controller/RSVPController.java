package com.sparktech.happyendings.controller;

import com.sparktech.happyendings.dto.ApiResponse;
import com.sparktech.happyendings.dto.RsvpRequest;
import com.sparktech.happyendings.model.Guest;
import com.sparktech.happyendings.model.enums.InvitationRole;
import com.sparktech.happyendings.security.RequiresInvitationRole;
import com.sparktech.happyendings.service.RSVPService;
import com.sparktech.happyendings.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/invitations")
public class RSVPController {

    @Autowired
    private RSVPService rsvpService;

    @Autowired
    private RedisService redisService;

    @PostMapping("/{invitationId}/rsvp")
    @RequiresInvitationRole({InvitationRole.COUPLE, InvitationRole.COORDINATOR, InvitationRole.GUEST})
    public ResponseEntity<ApiResponse<?>> submitRsvp(
            @PathVariable Long invitationId,
            @RequestBody RsvpRequest rsvpRequest,
            HttpServletRequest request) {

        // Rate limiting key using client IP address
        String clientIp = request.getRemoteAddr();
        String limitKey = "ratelimit:rsvp:" + clientIp;

        // Limit to 5 RSVP actions per minute
        if (redisService.isRateLimited(limitKey, 5, 60)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Too many requests. Please try again after some time."));
        }

        try {
            Guest updatedGuest = rsvpService.updateRsvp(invitationId, rsvpRequest);
            return ResponseEntity.ok(ApiResponse.success(updatedGuest));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to submit RSVP"));
        }
    }

    @PostMapping("/rsvp/auth")
    public ResponseEntity<ApiResponse<com.sparktech.happyendings.dto.AuthResponse>> authGuestToken(@RequestBody java.util.Map<String, String> body) {
        String token = body.get("token");
        try {
            String guestJwt = rsvpService.authenticateGuestToken(token);
            return ResponseEntity.ok(ApiResponse.success(new com.sparktech.happyendings.dto.AuthResponse(guestJwt)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
        }
    }
}
