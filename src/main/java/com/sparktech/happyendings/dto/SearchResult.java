package com.sparktech.happyendings.dto;

import com.sparktech.happyendings.model.Guest;
import com.sparktech.happyendings.model.Invitation;
import com.sparktech.happyendings.model.User;
import com.sparktech.happyendings.model.WeddingTemplate;

import java.util.List;

public class SearchResult {
    private List<User> users;
    private List<Invitation> invitations;
    private List<Guest> guests;
    private List<WeddingTemplate> templates;

    public SearchResult() {}

    public SearchResult(List<User> users, List<Invitation> invitations, List<Guest> guests, List<WeddingTemplate> templates) {
        this.users = users;
        this.invitations = invitations;
        this.guests = guests;
        this.templates = templates;
    }

    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }

    public List<Invitation> getInvitations() { return invitations; }
    public void setInvitations(List<Invitation> invitations) { this.invitations = invitations; }

    public List<Guest> getGuests() { return guests; }
    public void setGuests(List<Guest> guests) { this.guests = guests; }

    public List<WeddingTemplate> getTemplates() { return templates; }
    public void setTemplates(List<WeddingTemplate> templates) { this.templates = templates; }
}
