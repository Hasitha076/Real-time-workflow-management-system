package edu.IIT.task_management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private int assignerId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    private TaskPriorityLevel priority;
    private int projectId;
    private List<Integer> collaboratorIds;
    private String createdAt;
    private String updatedAt;

}
