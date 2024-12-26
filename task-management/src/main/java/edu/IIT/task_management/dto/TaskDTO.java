package edu.IIT.task_management.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class TaskDTO {
    private int taskId;
    private String taskName;
    private String description;
    private boolean status;
    private int assigneeId;
    private TaskPriorityLevel priority;
    private int projectId;
    private List<Integer> collaboratorIds;
    private String createdAt;
    private String updatedAt;

}
