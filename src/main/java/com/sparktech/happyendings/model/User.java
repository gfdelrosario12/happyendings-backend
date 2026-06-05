package com.sparktech.happyendings.model;

import com.sparktech.happyendings.model.enums.Role;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users") // "user" is a reserved word in many SQL databases
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String gender;
    private String email;
    private int age;

    @Enumerated(EnumType.STRING)
    private Role role; // Global role (ADMIN, REGISTERED_USER)

    @OneToMany(mappedBy = "user")
    private List<InvitationUser> invitations;

    public User() {}

    public User(String gender, String email, int age, Role role) {
        this.gender = gender;
        this.email = email;
        this.age = age;
        this.role = role;
    }

    // Getters and Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public List<InvitationUser> getInvitations() { return invitations; }
    public void setInvitations(List<InvitationUser> invitations) { this.invitations = invitations; }
}