package com.sparktech.happyendings.model;

import jakarta.persistence.*;

@Entity
public class AssignedPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id") // Can be null if guest is not a registered user
    private User user;

    private String guestEmail; // For non-registered guests
    private String roleInSegment;
    private String notes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public String getRoleInSegment() {
        return roleInSegment;
    }

    public void setRoleInSegment(String roleInSegment) {
        this.roleInSegment = roleInSegment;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}