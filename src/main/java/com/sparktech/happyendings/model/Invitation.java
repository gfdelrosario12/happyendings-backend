package com.sparktech.happyendings.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "invitation")
    private List<InvitationUser> couple;

    @Embedded
    private CeremonyDetails ceremonyDetails;

    @Embedded
    private ReceptionDetails receptionDetails;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "invitation_id")
    private List<ProgramSegment> eventProgram;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "invitation_id")
    private List<GuestRoleGroup> guestRoleGroups;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "invitation_id")
    private List<Guest> guests;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<InvitationUser> getCouple() {
        return couple;
    }

    public void setCouple(List<InvitationUser> couple) {
        this.couple = couple;
    }

    public CeremonyDetails getCeremonyDetails() {
        return ceremonyDetails;
    }

    public void setCeremonyDetails(CeremonyDetails ceremonyDetails) {
        this.ceremonyDetails = ceremonyDetails;
    }

    public ReceptionDetails getReceptionDetails() {
        return receptionDetails;
    }

    public void setReceptionDetails(ReceptionDetails receptionDetails) {
        this.receptionDetails = receptionDetails;
    }

    public List<ProgramSegment> getEventProgram() {
        return eventProgram;
    }

    public void setEventProgram(List<ProgramSegment> eventProgram) {
        this.eventProgram = eventProgram;
    }

    public List<GuestRoleGroup> getGuestRoleGroups() {
        return guestRoleGroups;
    }

    public void setGuestRoleGroups(List<GuestRoleGroup> guestRoleGroups) {
        this.guestRoleGroups = guestRoleGroups;
    }

    public List<Guest> getGuests() {
        return guests;
    }

    public void setGuests(List<Guest> guests) {
        this.guests = guests;
    }
}