package com.sparktech.happyendings.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class GuestRoleGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @ElementCollection
    @CollectionTable(name = "guest_role_group_emails", joinColumns = @JoinColumn(name = "role_group_id"))
    @Column(name = "guest_email")
    private List<String> assignedGuestEmails;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getAssignedGuestEmails() {
        return assignedGuestEmails;
    }

    public void setAssignedGuestEmails(List<String> assignedGuestEmails) {
        this.assignedGuestEmails = assignedGuestEmails;
    }
}