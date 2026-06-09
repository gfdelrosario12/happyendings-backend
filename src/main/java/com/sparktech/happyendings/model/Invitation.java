package com.sparktech.happyendings.model;

import com.sparktech.happyendings.model.enums.InvitationStatus;
import com.sparktech.happyendings.model.enums.InvitationVisibility;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long invitationTemplateId; // template ID reference

    private String title;
    private String slug;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status = InvitationStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    private InvitationVisibility visibility = InvitationVisibility.PUBLIC;

    private LocalDateTime weddingDate;
    private String timezone;
    private String language;
    private String theme;
    private String template;

    private LocalDateTime publishedAt;
    private LocalDateTime archivedAt;

    private boolean deleted = false;
    private LocalDateTime deletedAt;

    @Version
    private Long version; // Optimistic concurrency protection

    @ManyToOne
    @JoinColumn(name = "partner_a_id")
    private User partnerA;

    @ManyToOne
    @JoinColumn(name = "partner_b_id")
    private User partnerB;

    @OneToMany(mappedBy = "invitation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvitationUser> members; // Renamed from couple for clarity since it holds couples, coordinators etc

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "venueName", column = @Column(name = "ceremony_venue_name")),
        @AttributeOverride(name = "address", column = @Column(name = "ceremony_address")),
        @AttributeOverride(name = "dateTime", column = @Column(name = "ceremony_date_time")),
        @AttributeOverride(name = "officiant", column = @Column(name = "ceremony_officiant"))
    })
    private CeremonyDetails ceremonyDetails;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "venueName", column = @Column(name = "reception_venue_name")),
        @AttributeOverride(name = "address", column = @Column(name = "reception_address")),
        @AttributeOverride(name = "dateTime", column = @Column(name = "reception_date_time")),
        @AttributeOverride(name = "additionalInstructions", column = @Column(name = "reception_additional_instructions"))
    })
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

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInvitationTemplateId() { return invitationTemplateId; }
    public void setInvitationTemplateId(Long invitationTemplateId) { this.invitationTemplateId = invitationTemplateId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public InvitationStatus getStatus() { return status; }
    public void setStatus(InvitationStatus status) { this.status = status; }

    public InvitationVisibility getVisibility() { return visibility; }
    public void setVisibility(InvitationVisibility visibility) { this.visibility = visibility; }

    public LocalDateTime getWeddingDate() { return weddingDate; }
    public void setWeddingDate(LocalDateTime weddingDate) { this.weddingDate = weddingDate; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public LocalDateTime getArchivedAt() { return archivedAt; }
    public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public User getPartnerA() { return partnerA; }
    public void setPartnerA(User partnerA) { this.partnerA = partnerA; }

    public User getPartnerB() { return partnerB; }
    public void setPartnerB(User partnerB) { this.partnerB = partnerB; }

    public List<InvitationUser> getMembers() { return members; }
    public void setMembers(List<InvitationUser> members) { this.members = members; }

    // Backward compatibility getters for "couple"
    public List<InvitationUser> getCouple() { return members; }
    public void setCouple(List<InvitationUser> couple) { this.members = couple; }

    public CeremonyDetails getCeremonyDetails() { return ceremonyDetails; }
    public void setCeremonyDetails(CeremonyDetails ceremonyDetails) { this.ceremonyDetails = ceremonyDetails; }

    public ReceptionDetails getReceptionDetails() { return receptionDetails; }
    public void setReceptionDetails(ReceptionDetails receptionDetails) { this.receptionDetails = receptionDetails; }

    public List<ProgramSegment> getEventProgram() { return eventProgram; }
    public void setEventProgram(List<ProgramSegment> eventProgram) { this.eventProgram = eventProgram; }

    public List<GuestRoleGroup> getGuestRoleGroups() { return guestRoleGroups; }
    public void setGuestRoleGroups(List<GuestRoleGroup> guestRoleGroups) { this.guestRoleGroups = guestRoleGroups; }

    public List<Guest> getGuests() { return guests; }
    public void setGuests(List<Guest> guests) { this.guests = guests; }

    // Convenience aliases
    public User getBride() { return partnerA; }
    public void setBride(User bride) { this.partnerA = bride; }
    public User getGroom() { return partnerB; }
    public void setGroom(User groom) { this.partnerB = groom; }
}