package com.sparktech.happyendings.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class GuestRoleGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invitation_id", insertable = false, updatable = false)
    private Long invitationId;
    private String name;
    private String description;
    private String colorTag; // e.g. #FF5733 or "blue"
    private int orderIndex;

    @ElementCollection
    @CollectionTable(name = "guest_role_group_emails", joinColumns = @JoinColumn(name = "role_group_id"))
    @Column(name = "guest_email")
    private List<String> assignedGuestEmails;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInvitationId() { return invitationId; }
    public void setInvitationId(Long invitationId) { this.invitationId = invitationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getColorTag() { return colorTag; }
    public void setColorTag(String colorTag) { this.colorTag = colorTag; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public List<String> getAssignedGuestEmails() { return assignedGuestEmails; }
    public void setAssignedGuestEmails(List<String> assignedGuestEmails) { this.assignedGuestEmails = assignedGuestEmails; }
}