package com.sparktech.happyendings.service;

import com.sparktech.happyendings.model.*;
import com.sparktech.happyendings.model.enums.InvitationRole;
import com.sparktech.happyendings.model.enums.InvitationStatus;
import com.sparktech.happyendings.model.enums.InvitationVisibility;
import com.sparktech.happyendings.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InvitationService {

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private InvitationUserRepository invitationUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProgramSegmentRepository programSegmentRepository;

    @Autowired
    private GuestRoleGroupRepository guestRoleGroupRepository;

    @Autowired
    private AssignedPersonRepository assignedPersonRepository;

    @Autowired
    private ActionLogService actionLogService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Transactional
    public Invitation createInvitation(Invitation invitation, User creator) {
        invitation.setPartnerA(creator);
        invitation.setStatus(InvitationStatus.DRAFT);
        invitation.setDeleted(false);
        if (invitation.getSlug() == null) {
            invitation.setSlug("wedding-" + System.currentTimeMillis());
        }

        Invitation savedInvitation = invitationRepository.save(invitation);

        InvitationUser invitationUser = new InvitationUser();
        invitationUser.setInvitation(savedInvitation);
        invitationUser.setUser(creator);
        invitationUser.setRole(InvitationRole.COUPLE);
        invitationUserRepository.save(invitationUser);

        actionLogService.logAction(creator.getId(), "INVITATION_CREATED", "Invitation created with ID: " + savedInvitation.getId());
        kafkaProducerService.sendEvent("invitation-events", "InvitationCreated:" + savedInvitation.getId());
        return savedInvitation;
    }

    public Optional<Invitation> getInvitationById(Long id) {
        // Simple caching in Redis
        String cacheKey = "invitation:details:" + id;
        Invitation cached = redisService.get(cacheKey, Invitation.class);
        if (cached != null) {
            return Optional.of(cached);
        }

        Optional<Invitation> invitation = invitationRepository.findById(id);
        invitation.ifPresent(i -> redisService.set(cacheKey, i, 300)); // cache for 5 min
        return invitation;
    }

    public Optional<Invitation> getInvitationBySlug(String slug) {
        return invitationRepository.findBySlugAndDeletedFalse(slug);
    }

    public List<Invitation> listUserInvitations(Long userId) {
        return invitationRepository.findActiveByPartnerId(userId);
    }

    @Transactional
    public Invitation publishInvitation(Long invitationId, Long userId) {
        String lockKey = "lock:publish:" + invitationId;
        // Acquire Redis distributed lock for 10 seconds
        if (!redisService.acquireLock(lockKey, 10)) {
            throw new IllegalStateException("Another publish operation is in progress for this invitation.");
        }

        try {
            Invitation invitation = invitationRepository.findById(invitationId)
                    .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));

            invitation.setStatus(InvitationStatus.PUBLISHED);
            invitation.setPublishedAt(LocalDateTime.now());
            Invitation saved = invitationRepository.save(invitation);

            // Invalidate cache
            redisService.delete("invitation:details:" + invitationId);

            actionLogService.logAction(userId, "INVITATION_PUBLISHED", "Invitation published with ID: " + invitationId);
            kafkaProducerService.sendEvent("invitation-events", "InvitationPublished:" + invitationId);

            return saved;
        } finally {
            redisService.releaseLock(lockKey);
        }
    }

    @Transactional
    public Invitation archiveInvitation(Long invitationId, Long userId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));

        invitation.setStatus(InvitationStatus.ARCHIVED);
        invitation.setArchivedAt(LocalDateTime.now());
        Invitation saved = invitationRepository.save(invitation);

        redisService.delete("invitation:details:" + invitationId);

        actionLogService.logAction(userId, "INVITATION_ARCHIVED", "Invitation archived with ID: " + invitationId);
        kafkaProducerService.sendEvent("invitation-events", "InvitationArchived:" + invitationId);

        return saved;
    }

    @Transactional
    public void softDeleteInvitation(Long invitationId, Long userId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));

        invitation.setDeleted(true);
        invitation.setDeletedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        redisService.delete("invitation:details:" + invitationId);

        actionLogService.logAction(userId, "INVITATION_SOFT_DELETED", "Invitation soft deleted with ID: " + invitationId);
        kafkaProducerService.sendEvent("invitation-events", "InvitationDeleted:" + invitationId);
    }

    @Transactional
    public void restoreInvitation(Long invitationId, Long userId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));

        invitation.setDeleted(false);
        invitation.setDeletedAt(null);
        invitationRepository.save(invitation);

        actionLogService.logAction(userId, "INVITATION_RESTORED", "Invitation restored with ID: " + invitationId);
        kafkaProducerService.sendEvent("invitation-events", "InvitationRestored:" + invitationId);
    }

    @Transactional
    public Invitation updateCeremonyAndReception(Long invitationId, CeremonyDetails ceremony, ReceptionDetails reception, Long userId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));

        invitation.setCeremonyDetails(ceremony);
        invitation.setReceptionDetails(reception);
        Invitation saved = invitationRepository.save(invitation);

        redisService.delete("invitation:details:" + invitationId);

        actionLogService.logAction(userId, "INVITATION_UPDATED", "Invitation event details updated.");
        kafkaProducerService.sendEvent("invitation-events", "InvitationUpdated:" + invitationId);

        return saved;
    }

    // Dynamic Program Timelines
    @Transactional
    public ProgramSegment addProgramSegment(Long invitationId, ProgramSegment segment, Long userId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));

        segment.setInvitationId(invitationId);
        if (invitation.getEventProgram() == null) {
            invitation.setEventProgram(new ArrayList<>());
        }
        segment.setOrderIndex(invitation.getEventProgram().size());
        segment.setCreatedBy(userId);
        segment.setUpdatedBy(userId);

        ProgramSegment savedSegment = programSegmentRepository.save(segment);
        invitation.getEventProgram().add(savedSegment);
        invitationRepository.save(invitation);

        redisService.delete("invitation:details:" + invitationId);

        actionLogService.logAction(userId, "PROGRAM_SEGMENT_ADDED", "Added segment: " + segment.getTitle());
        kafkaProducerService.sendEvent("invitation-events", "ProgramUpdated:" + invitationId);
        return savedSegment;
    }

    @Transactional
    public ProgramSegment updateProgramSegment(Long segmentId, ProgramSegment updateData, Long userId) {
        ProgramSegment segment = programSegmentRepository.findById(segmentId)
                .orElseThrow(() -> new IllegalArgumentException("Segment not found."));

        segment.setTitle(updateData.getTitle());
        segment.setDescription(updateData.getDescription());
        segment.setStartTime(updateData.getStartTime());
        segment.setDuration(updateData.getDuration());
        segment.setVisibility(updateData.getVisibility());
        segment.setParentId(updateData.getParentId());
        segment.setPublished(updateData.isPublished());
        segment.setUpdatedBy(userId);

        ProgramSegment saved = programSegmentRepository.save(segment);

        redisService.delete("invitation:details:" + segment.getInvitationId());

        actionLogService.logAction(userId, "PROGRAM_SEGMENT_UPDATED", "Updated segment: " + segment.getTitle());
        kafkaProducerService.sendEvent("invitation-events", "ProgramUpdated:" + segment.getInvitationId());
        return saved;
    }

    @Transactional
    public void deleteProgramSegment(Long segmentId, Long userId) {
        ProgramSegment segment = programSegmentRepository.findById(segmentId)
                .orElseThrow(() -> new IllegalArgumentException("Segment not found."));
        Long invitationId = segment.getInvitationId();

        programSegmentRepository.delete(segment);

        redisService.delete("invitation:details:" + invitationId);

        actionLogService.logAction(userId, "PROGRAM_SEGMENT_DELETED", "Deleted segment ID: " + segmentId);
        kafkaProducerService.sendEvent("invitation-events", "ProgramUpdated:" + invitationId);
    }

    @Transactional
    public void reorderProgramSegments(Long invitationId, List<Long> segmentIds, Long userId) {
        String lockKey = "lock:reorder:" + invitationId;
        if (!redisService.acquireLock(lockKey, 10)) {
            throw new IllegalStateException("Reordering is already in progress.");
        }

        try {
            for (int i = 0; i < segmentIds.size(); i++) {
                Long segmentId = segmentIds.get(i);
                ProgramSegment segment = programSegmentRepository.findById(segmentId)
                        .orElseThrow(() -> new IllegalArgumentException("Segment not found: " + segmentId));
                segment.setOrderIndex(i);
                programSegmentRepository.save(segment);
            }

            redisService.delete("invitation:details:" + invitationId);

            actionLogService.logAction(userId, "PROGRAM_REORDERED", "Reordered segments for invitation: " + invitationId);
            kafkaProducerService.sendEvent("invitation-events", "ProgramUpdated:" + invitationId);
        } finally {
            redisService.releaseLock(lockKey);
        }
    }

    // Guest Role Groups
    @Transactional
    public GuestRoleGroup addGuestRoleGroup(Long invitationId, GuestRoleGroup group, Long userId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));

        group.setInvitationId(invitationId);
        if (invitation.getGuestRoleGroups() == null) {
            invitation.setGuestRoleGroups(new ArrayList<>());
        }
        group.setOrderIndex(invitation.getGuestRoleGroups().size());

        GuestRoleGroup saved = guestRoleGroupRepository.save(group);
        invitation.getGuestRoleGroups().add(saved);
        invitationRepository.save(invitation);

        actionLogService.logAction(userId, "ROLE_GROUP_CREATED", "Created role group: " + group.getName());
        kafkaProducerService.sendEvent("invitation-events", "RoleGroupUpdated:" + invitationId);
        return saved;
    }

    @Transactional
    public GuestRoleGroup updateGuestRoleGroup(Long groupId, GuestRoleGroup updateData, Long userId) {
        GuestRoleGroup group = guestRoleGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found."));

        group.setName(updateData.getName());
        group.setDescription(updateData.getDescription());
        group.setColorTag(updateData.getColorTag());
        group.setAssignedGuestEmails(updateData.getAssignedGuestEmails());

        GuestRoleGroup saved = guestRoleGroupRepository.save(group);

        actionLogService.logAction(userId, "ROLE_GROUP_UPDATED", "Updated role group: " + group.getName());
        kafkaProducerService.sendEvent("invitation-events", "RoleGroupUpdated:" + group.getInvitationId());
        return saved;
    }

    @Transactional
    public void deleteGuestRoleGroup(Long groupId, Long userId) {
        GuestRoleGroup group = guestRoleGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found."));
        Long invitationId = group.getInvitationId();

        guestRoleGroupRepository.delete(group);

        actionLogService.logAction(userId, "ROLE_GROUP_DELETED", "Deleted role group ID: " + groupId);
        kafkaProducerService.sendEvent("invitation-events", "RoleGroupUpdated:" + invitationId);
    }

    // Program Assignment Engine
    @Transactional
    public AssignedPerson assignParticipantToSegment(Long segmentId, AssignedPerson assignment, Long userId) {
        ProgramSegment segment = programSegmentRepository.findById(segmentId)
                .orElseThrow(() -> new IllegalArgumentException("Segment not found."));

        assignment.setProgramSegmentId(segmentId);
        if (segment.getAssignedPersons() == null) {
            segment.setAssignedPersons(new ArrayList<>());
        }

        // Link registered User if available
        if (assignment.getParticipantType().equals("COUPLE") || assignment.getParticipantType().equals("COORDINATOR")) {
            userRepository.findById(assignment.getParticipantId()).ifPresent(assignment::setUser);
        }

        AssignedPerson saved = assignedPersonRepository.save(assignment);
        segment.getAssignedPersons().add(saved);
        programSegmentRepository.save(segment);

        actionLogService.logAction(userId, "PARTICIPANT_ASSIGNED", "Assigned role '" + assignment.getRoleName() + "' in segment " + segment.getTitle());
        kafkaProducerService.sendEvent("invitation-events", "CoordinatorAssigned:" + segment.getInvitationId());
        return saved;
    }

    @Transactional
    public void removeParticipantFromSegment(Long segmentId, Long assignmentId, Long userId) {
        ProgramSegment segment = programSegmentRepository.findById(segmentId)
                .orElseThrow(() -> new IllegalArgumentException("Segment not found."));

        AssignedPerson assignment = assignedPersonRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found."));

        segment.getAssignedPersons().remove(assignment);
        programSegmentRepository.save(segment);
        assignedPersonRepository.delete(assignment);

        actionLogService.logAction(userId, "PARTICIPANT_UNASSIGNED", "Removed assignment ID: " + assignmentId);
        kafkaProducerService.sendEvent("invitation-events", "ProgramUpdated:" + segment.getInvitationId());
    }

    @Transactional
    public void addCoordinator(Long invitationId, String email, Long userId) {
        User coordinator = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User with email " + email + " is not registered on the platform."));

        Optional<InvitationUser> existing = invitationUserRepository.findByInvitationIdAndUserId(invitationId, coordinator.getId());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("User is already a member of this invitation.");
        }

        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));

        InvitationUser member = new InvitationUser();
        member.setInvitation(invitation);
        member.setUser(coordinator);
        member.setRole(InvitationRole.COORDINATOR);
        invitationUserRepository.save(member);

        actionLogService.logAction(userId, "COORDINATOR_ADDED", "Added coordinator: " + email + " to invitation " + invitationId);
        kafkaProducerService.sendEvent("invitation-events", "CoordinatorAssigned:" + invitationId);
    }
}