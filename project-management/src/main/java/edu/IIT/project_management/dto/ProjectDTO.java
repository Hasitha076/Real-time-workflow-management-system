package edu.IIT.project_management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class ProjectDTO {
    private int projectId;
    private String projectName;
    private ProjectPriorityLevel priority;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    private List<Integer> collaboratorIds;
    private ProjectStatus status = ProjectStatus.PENDING;
    private String createdAt;
    private String updatedAt;

}
