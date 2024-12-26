package edu.IIT.task_management.model;

import edu.IIT.task_management.dto.TaskPriorityLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
@Entity(name = "Task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int taskId;
    private String taskName;
    private String description;
    private boolean status = false;
    private int assigneeId;

    @Enumerated(EnumType.STRING)
    private TaskPriorityLevel priority;

    private int projectId;

    private List<Integer> collaboratorIds;

    @Column(updatable = false)
    private String createdAt;

    private String updatedAt;

    @PrePersist
    protected void onCreate() {
        String currentTimestamp = getCurrentTimestamp();
        this.createdAt = currentTimestamp;
        this.updatedAt = currentTimestamp;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = getCurrentTimestamp();
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}