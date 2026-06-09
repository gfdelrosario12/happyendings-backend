package com.sparktech.happyendings.model;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.List;

@Entity
public class ProgramSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invitation_id", insertable = false, updatable = false)
    private Long invitationId;
    private String title;
    private String description;
    private LocalTime startTime;
    private Integer duration;
    private int orderIndex;
    private String visibility = "PUBLIC"; // PUBLIC, PRIVATE, COORDINATOR_ONLY
    private Long createdBy;
    private Long updatedBy;
    private Long parentId; // For nested sections/group segments
    private boolean published = true;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "program_segment_id")
    private List<AssignedPerson> assignedPersons;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInvitationId() { return invitationId; }
    public void setInvitationId(Long invitationId) { this.invitationId = invitationId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public List<AssignedPerson> getAssignedPersons() { return assignedPersons; }
    public void setAssignedPersons(List<AssignedPerson> assignedPersons) { this.assignedPersons = assignedPersons; }
}