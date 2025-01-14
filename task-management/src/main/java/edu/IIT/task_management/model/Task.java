package edu.IIT.task_management.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.IIT.task_management.dto.TaskPriorityLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int taskId;

    private String taskName;
    private String description;
    private boolean status;
    private int assignerId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private TaskPriorityLevel priority;

    private int projectId;
    private int workId;

    @ElementCollection
    @CollectionTable(name = "task_collaborators", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "collaborator_id")
    private List<Integer> collaboratorIds;

    @ElementCollection
    @CollectionTable(name = "task_teams", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "team_id")
    private List<Integer> teamIds;

    @ElementCollection
    @CollectionTable(name = "task_memberIcons", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "member_id")
    private List<String> memberIcons;

    @ElementCollection
    @CollectionTable(name = "task_tags", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "tag_id")
    private List<String> tags;

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
