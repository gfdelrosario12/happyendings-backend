package com.sparktech.happyendings.dto;

import com.sparktech.happyendings.model.enums.RsvpStatus;

public class RsvpRequest {
    private Long guestId;
    private RsvpStatus status;
    private String additionalNotes;

    public RsvpRequest() {}

    public RsvpRequest(Long guestId, RsvpStatus status, String additionalNotes) {
        this.guestId = guestId;
        this.status = status;
        this.additionalNotes = additionalNotes;
    }

    public Long getGuestId() {
        return guestId;
    }

    public void setGuestId(Long guestId) {
        this.guestId = guestId;
    }

    public RsvpStatus getStatus() {
        return status;
    }

    public void setStatus(RsvpStatus status) {
        this.status = status;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }
}
