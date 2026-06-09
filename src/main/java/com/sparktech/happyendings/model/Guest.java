package com.sparktech.happyendings.model;

import com.sparktech.happyendings.model.enums.RsvpStatus;
import jakarta.persistence.*;

@Entity
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String name; // Backwards compatibility
    private String firstName;
    private String lastName;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private RsvpStatus rsvpStatus = RsvpStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "role_group_id")
    private GuestRoleGroup assignedRoleGroup;

    @Column(name = "plus_one_allowance")
    private boolean plusOneAllowed;
    private String plusOneName;
    private String dietaryRestrictions;
    private int attendanceCount;
    private String additionalNotes;

    @ManyToOne
    @JoinColumn(name = "user_id") // Optional link to registered user account
    private User linkedUser;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public RsvpStatus getRsvpStatus() { return rsvpStatus; }
    public void setRsvpStatus(RsvpStatus rsvpStatus) { this.rsvpStatus = rsvpStatus; }

    public GuestRoleGroup getAssignedRoleGroup() { return assignedRoleGroup; }
    public void setAssignedRoleGroup(GuestRoleGroup assignedRoleGroup) { this.assignedRoleGroup = assignedRoleGroup; }

    public boolean isPlusOneAllowed() { return plusOneAllowed; }
    public void setPlusOneAllowed(boolean plusOneAllowed) { this.plusOneAllowed = plusOneAllowed; }

    // Backward compatibility getter/setter for plusOneAllowance
    public boolean isPlusOneAllowance() { return plusOneAllowed; }
    public void setPlusOneAllowance(boolean plusOneAllowance) { this.plusOneAllowed = plusOneAllowance; }

    public String getPlusOneName() { return plusOneName; }
    public void setPlusOneName(String plusOneName) { this.plusOneName = plusOneName; }

    public String getDietaryRestrictions() { return dietaryRestrictions; }
    public void setDietaryRestrictions(String dietaryRestrictions) { this.dietaryRestrictions = dietaryRestrictions; }

    public int getAttendanceCount() { return attendanceCount; }
    public void setAttendanceCount(int attendanceCount) { this.attendanceCount = attendanceCount; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }

    public User getLinkedUser() { return linkedUser; }
    public void setLinkedUser(User linkedUser) { this.linkedUser = linkedUser; }
}