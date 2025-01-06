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
    private boolean status;
    private int assignerId;

    @Enumerated(EnumType.STRING)
    private WorkPriorityLevel priority;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    private int projectId;

    @ElementCollection
    @CollectionTable(name = "work_collaborators", joinColumns = @JoinColumn(name = "work_id"))
    @Column(name = "collaborator_id")
    private List<Integer> collaboratorIds;

    @ElementCollection
    @CollectionTable(name = "work_teams", joinColumns = @JoinColumn(name = "work_id"))
    @Column(name = "team_id")
    private List<Integer> teamIds;

    @ElementCollection
    @CollectionTable(name = "work_memberIcons", joinColumns = @JoinColumn(name = "work_id"))
    @Column(name = "member_id")
    private List<String> memberIcons;

    @ElementCollection
    @CollectionTable(name = "work_tags", joinColumns = @JoinColumn(name = "work_id"))
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
