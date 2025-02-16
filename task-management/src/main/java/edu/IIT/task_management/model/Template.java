package edu.IIT.task_management.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.IIT.task_management.dto.TaskPriorityLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "template")
public class Template {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int taskTemplateId;

    private String taskTemplateName;
    private String taskTemplateDescription;
    private boolean taskTemplateStatus;
    private int assignerId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate taskTemplateDueDate;

    @Enumerated(EnumType.STRING)
    private TaskPriorityLevel taskTemplatePriority;

    private int projectId;

    @ElementCollection
    @CollectionTable(name = "template_collaborators", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "template_collaborator_id")
    private List<Integer> taskTemplateCollaboratorIds;

    @ElementCollection
    @CollectionTable(name = "template_teams", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "template_team_id")
    private List<Integer> taskTemplateTeamIds;

    @ElementCollection
    @CollectionTable(name = "template_tags", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "template_tag_id")
    private List<String> taskTemplateTags;

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
