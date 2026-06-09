package com.sparktech.happyendings;

import com.sparktech.happyendings.dto.RegisterRequest;
import com.sparktech.happyendings.dto.RsvpRequest;
import com.sparktech.happyendings.dto.SearchResult;
import com.sparktech.happyendings.model.*;
import com.sparktech.happyendings.model.enums.AccountStatus;
import com.sparktech.happyendings.model.enums.RsvpStatus;
import com.sparktech.happyendings.service.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@Transactional
class HappyendingsApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private GuestService guestService;

    @Autowired
    private RSVPService rsvpService;

    @Autowired
    private WeddingTemplateService templateService;

    @Autowired
    private SearchService searchService;

    @Test
    void contextLoads() {
        Assertions.assertNotNull(userService);
        Assertions.assertNotNull(invitationService);
        Assertions.assertNotNull(guestService);
        Assertions.assertNotNull(rsvpService);
        Assertions.assertNotNull(templateService);
        Assertions.assertNotNull(searchService);
    }

    @Test
    void testUserWorkflows() {
        String email = "test" + System.currentTimeMillis() + "@example.com";
        RegisterRequest req = new RegisterRequest();
        req.setName("John Doe");
        req.setEmail(email);
        req.setPassword("Password123!");

        User registeredUser = userService.registerUser(req);
        Assertions.assertNotNull(registeredUser.getId());
        Assertions.assertEquals("John", registeredUser.getFirstName());
        Assertions.assertEquals("Doe", registeredUser.getLastName());
        Assertions.assertEquals(AccountStatus.ACTIVE, registeredUser.getAccountStatus());

        // Update profile
        User updated = userService.updateUserProfile(registeredUser.getId(), "Johnny", "Doe", "+123456", "photo.png");
        Assertions.assertEquals("Johnny Doe", updated.getName());
        Assertions.assertEquals("+123456", updated.getPhoneNumber());

        // Change status
        userService.changeAccountStatus(registeredUser.getId(), AccountStatus.SUSPENDED);
        User suspended = userService.getUserById(registeredUser.getId()).orElseThrow();
        Assertions.assertEquals(AccountStatus.SUSPENDED, suspended.getAccountStatus());
    }

    @Test
    void testInvitationTimelineAndRoleGroups() {
        String email = "couple" + System.currentTimeMillis() + "@example.com";
        RegisterRequest req = new RegisterRequest();
        req.setName("Bride Groom");
        req.setEmail(email);
        req.setPassword("SecurePass1!");
        User coupleUser = userService.registerUser(req);

        Invitation invitation = new Invitation();
        invitation.setTitle("Our Wedding Day");
        invitation.setSlug("bride-groom-" + System.currentTimeMillis());
        invitation.setWeddingDate(LocalDateTime.now().plusDays(30));

        Invitation created = invitationService.createInvitation(invitation, coupleUser);
        Assertions.assertNotNull(created.getId());
        Assertions.assertEquals(com.sparktech.happyendings.model.enums.InvitationStatus.DRAFT, created.getStatus());

        // Publish
        Invitation published = invitationService.publishInvitation(created.getId(), coupleUser.getId());
        Assertions.assertEquals(com.sparktech.happyendings.model.enums.InvitationStatus.PUBLISHED, published.getStatus());

        // Program timeline segment
        ProgramSegment segment = new ProgramSegment();
        segment.setTitle("Opening Toast");
        segment.setDescription("First toast from the Best Man");
        segment.setDuration(15);
        ProgramSegment savedSegment = invitationService.addProgramSegment(published.getId(), segment, coupleUser.getId());
        Assertions.assertNotNull(savedSegment.getId());

        // Assignment
        AssignedPerson assignment = new AssignedPerson();
        assignment.setParticipantId(coupleUser.getId());
        assignment.setParticipantType("COUPLE");
        assignment.setRoleName("Toast Host");
        AssignedPerson savedAssignment = invitationService.assignParticipantToSegment(savedSegment.getId(), assignment, coupleUser.getId());
        Assertions.assertNotNull(savedAssignment.getId());
    }

    @Test
    void testGuestCSVImportAndRsvpFlow() {
        String email = "host" + System.currentTimeMillis() + "@example.com";
        RegisterRequest req = new RegisterRequest();
        req.setName("Wedding Host");
        req.setEmail(email);
        req.setPassword("SecurePass1!");
        User hostUser = userService.registerUser(req);

        Invitation invitation = new Invitation();
        invitation.setTitle("Golden Wedding");
        invitation.setSlug("golden-" + System.currentTimeMillis());
        invitation.setWeddingDate(LocalDateTime.now().plusDays(60));
        Invitation created = invitationService.createInvitation(invitation, hostUser);

        // Bulk CSV import
        String csv = "email,firstName,lastName,phoneNumber,plusOneAllowed,plusOneName,dietaryRestrictions,notes\n" +
                "alice@example.com,Alice,Smith,+19876,true,Bob,Vegan,None\n" +
                "charlie@example.com,Charlie,Brown,+1222,false,,None,None";

        List<Guest> imported = guestService.importGuests(created.getId(), csv, hostUser.getId());
        Assertions.assertEquals(2, imported.size());

        Guest alice = imported.stream().filter(g -> g.getEmail().equals("alice@example.com")).findFirst().orElseThrow();
        Assertions.assertTrue(alice.isPlusOneAllowed());

        // RSVP submit
        RsvpRequest rsvpReq = new RsvpRequest();
        rsvpReq.setGuestId(alice.getId());
        rsvpReq.setStatus(RsvpStatus.ACCEPTED);
        rsvpReq.setAttendanceCount(2);
        rsvpReq.setPlusOneName("Bob");
        rsvpReq.setDietaryRestrictions("Vegan");

        Guest rsvped = rsvpService.updateRsvp(created.getId(), rsvpReq);
        Assertions.assertEquals(RsvpStatus.ACCEPTED, rsvped.getRsvpStatus());
        Assertions.assertEquals(2, rsvped.getAttendanceCount());
    }

    @Test
    void testTemplateSystem() {
        WeddingTemplate template = new WeddingTemplate();
        template.setName("Classic Roses");
        template.setDescription("Elegant red roses theme");
        template.setCategory("Classic");
        template.setTags("floral, romantic");

        WeddingTemplate created = templateService.createTemplate(template, 1L);
        Assertions.assertNotNull(created.getId());

        // Clone
        WeddingTemplate cloned = templateService.cloneTemplate(created.getId(), "Cloned Roses", 1L);
        Assertions.assertNotNull(cloned.getId());
        Assertions.assertEquals(created.getId(), cloned.getClonedFromId());

        // Favorite toggle
        boolean faved = templateService.toggleTemplateFavorite(created.getId(), 1L);
        Assertions.assertTrue(faved);
    }

    @Test
    void testGlobalSearch() {
        // Create user to search
        RegisterRequest req = new RegisterRequest();
        req.setName("Searchable Tester");
        req.setEmail("searchable@example.com");
        req.setPassword("Password123!");
        userService.registerUser(req);

        SearchResult results = searchService.globalSearch("searchable");
        Assertions.assertFalse(results.getUsers().isEmpty());
    }
}
