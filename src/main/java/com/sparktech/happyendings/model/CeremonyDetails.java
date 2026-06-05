package com.sparktech.happyendings.model;

import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
public class CeremonyDetails {

    private String venueName;
    private String address;
    private LocalDateTime dateTime;
    private String officiant;

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getOfficiant() {
        return officiant;
    }

    public void setOfficiant(String officiant) {
        this.officiant = officiant;
    }
}