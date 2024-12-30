package edu.IIT.work_management.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.IIT.work_management.dto.WorkPriorityLevel;
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
@Table(name = "work")
public class Work {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int workId;

    private String workName;
    private String description;
    private boolean status = false;
    private int assignerId;

    @Enumerated(EnumType.STRING)
    private WorkPriorityLevel priority;

    private int projectId;

    @ElementCollection
    @CollectionTable(name = "work_collaborators", joinColumns = @JoinColumn(name = "work_id"))
    @Column(name = "collaborator_id")
    private List<Integer> collaboratorIds;

    @ElementCollection
    @CollectionTable(name = "work_collaborators", joinColumns = @JoinColumn(name = "work_id"))
    @Column(name = "team_id")
    private List<Integer> teamIds;

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
