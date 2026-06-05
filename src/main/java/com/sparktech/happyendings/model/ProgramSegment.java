package com.sparktech.happyendings.model;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.List;

@Entity
public class ProgramSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private LocalTime startTime;
    private Integer duration; // in minutes
    private int orderIndex;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "program_segment_id")
    private List<AssignedPerson> assignedPersons;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public List<AssignedPerson> getAssignedPersons() {
        return assignedPersons;
    }

    public void setAssignedPersons(List<AssignedPerson> assignedPersons) {
        this.assignedPersons = assignedPersons;
    }
}