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

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
@Entity(name = "Project")
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

    private List<Integer> collaboratorIds;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

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