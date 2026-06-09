package com.sparktech.happyendings.dto;

import com.sparktech.happyendings.model.enums.RsvpStatus;

public class RsvpRequest {
    private Long guestId;
    private RsvpStatus status;
    private String additionalNotes;

    private int attendanceCount;
    private String plusOneName;
    private String dietaryRestrictions;

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

    public int getAttendanceCount() {
        return attendanceCount;
    }

    public void setAttendanceCount(int attendanceCount) {
        this.attendanceCount = attendanceCount;
    }

    public String getPlusOneName() {
        return plusOneName;
    }

    public void setPlusOneName(String plusOneName) {
        this.plusOneName = plusOneName;
    }

    public String getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public void setDietaryRestrictions(String dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }
}
