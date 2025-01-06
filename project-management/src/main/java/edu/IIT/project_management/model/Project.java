package edu.IIT.project_management.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.IIT.project_management.dto.ProjectPriorityLevel;
import edu.IIT.project_management.dto.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "project")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int projectId;
    private String projectName;
    private String projectDescription;

    @Enumerated(EnumType.STRING)
    private ProjectPriorityLevel priority;

    @ElementCollection
    @CollectionTable(name = "project_tags", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "tag_id")
    private List<String> tags;

    private int assignerId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @ElementCollection
    @CollectionTable(name = "project_collaborators", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "collaborator_id")
    private List<Integer> collaboratorIds;

    @ElementCollection
    @CollectionTable(name = "project_teams", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "team_id")
    private List<Integer> teamIds;

    @ElementCollection
    @CollectionTable(name = "project_memberIcons", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "member_id")
    private List<String> memberIcons;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}