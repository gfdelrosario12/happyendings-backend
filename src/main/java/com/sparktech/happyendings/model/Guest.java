package com.sparktech.happyendings.model;

import com.sparktech.happyendings.model.enums.RsvpStatus;
import jakarta.persistence.*;

@Entity
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String name;

    @Enumerated(EnumType.STRING)
    private RsvpStatus rsvpStatus;

    @ManyToOne
    @JoinColumn(name = "role_group_id")
    private GuestRoleGroup assignedRoleGroup;

    private boolean plusOneAllowance;
    private String additionalNotes;

    @ManyToOne
    @JoinColumn(name = "user_id") // Optional link to registered user account
    private User linkedUser;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RsvpStatus getRsvpStatus() {
        return rsvpStatus;
    }

    public void setRsvpStatus(RsvpStatus rsvpStatus) {
        this.rsvpStatus = rsvpStatus;
    }

    public GuestRoleGroup getAssignedRoleGroup() {
        return assignedRoleGroup;
    }

    public void setAssignedRoleGroup(GuestRoleGroup assignedRoleGroup) {
        this.assignedRoleGroup = assignedRoleGroup;
    }

    public boolean isPlusOneAllowance() {
        return plusOneAllowance;
    }

    public void setPlusOneAllowance(boolean plusOneAllowance) {
        this.plusOneAllowance = plusOneAllowance;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public User getLinkedUser() {
        return linkedUser;
    }

    public void setLinkedUser(User linkedUser) {
        this.linkedUser = linkedUser;
    }
}