package edu.IIT.project_management.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.IIT.project_management.dto.ProjectPriorityLevel;
import edu.IIT.project_management.dto.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Enumerated(EnumType.STRING)
    private ProjectPriorityLevel priority;
    private int assignerId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @ElementCollection
    @CollectionTable(name = "project_collaborators", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "collaborator_id")
    private List<Integer> collaboratorIds;

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