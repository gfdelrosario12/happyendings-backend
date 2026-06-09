package com.sparktech.happyendings.model;

import jakarta.persistence.*;

@Entity
public class AssignedPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "program_segment_id", insertable = false, updatable = false)
    private Long programSegmentId;
    private Long participantId;
    private String participantType; // COUPLE, COORDINATOR, GUEST, SPONSOR, EXTERNAL
    private String roleName; // e.g. Speaker, Witness, Presenter, Reader
    private String notes;

    @ManyToOne
    @JoinColumn(name = "user_id") // Optional link to registered user
    private User user;

    private String guestEmail; // For backward compatibility / external guests

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProgramSegmentId() { return programSegmentId; }
    public void setProgramSegmentId(Long programSegmentId) { this.programSegmentId = programSegmentId; }

    public Long getParticipantId() { return participantId; }
    public void setParticipantId(Long participantId) { this.participantId = participantId; }

    public String getParticipantType() { return participantType; }
    public void setParticipantType(String participantType) { this.participantType = participantType; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    // Backward compatibility getter/setter for roleInSegment
    public String getRoleInSegment() { return roleName; }
    public void setRoleInSegment(String roleInSegment) { this.roleName = roleInSegment; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
}