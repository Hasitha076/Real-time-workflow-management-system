package edu.IIT.task_management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TemplateDTO {

    private int taskTemplateId;
    private String taskTemplateName;
    private String taskTemplateDescription;
    private boolean taskTemplateStatus= false;
    private int assignerId = 1;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate taskTemplateDueDate;

    private TaskPriorityLevel taskTemplatePriority;
    private int projectId;
    private List<Integer> taskTemplateCollaboratorIds;
    private List<Integer> taskTemplateTeamIds;
    private List<String> taskTemplateTags;
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}