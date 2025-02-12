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
public class TaskTemplateDTO {

    private int taskTemplateId;
    private String taskTemplateName;
    private String taskDescription;
    private boolean templateStatus= false;
    private int assignerId = 1;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate templateDueDate;

    private TaskPriorityLevel priority;
    private int projectId;
    private int workId;
    private List<Integer> collaboratorIds;
    private List<Integer> teamIds;
    private List<String> memberIcons;
    private List<String> tags;
    private List<String> comments;
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
