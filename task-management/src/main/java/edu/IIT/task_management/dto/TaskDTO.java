package edu.IIT.task_management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TaskDTO {
    private int taskId;
    private String taskName;
    private String description;
    private boolean status= false;
    private int assignerId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    private TaskPriorityLevel priority;
    private int projectId;
    private int workId;
    private List<Integer> collaboratorIds;
    private List<Integer> teamIds;
    private List<String> memberIcons;
    private List<String> tags;
    private List<TaskCommentDTO> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
