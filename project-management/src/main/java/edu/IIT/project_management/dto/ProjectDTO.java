package edu.IIT.project_management.dto;

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
public class ProjectDTO {
    private int projectId;
    private String projectName;
    private String projectDescription;
    private ProjectPriorityLevel priority;
    private int assignerId;

    private List<String> tags;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    private List<Integer> collaboratorIds;

    private List<Integer> teamIds;

    private List<String> memberIcons;


    private ProjectStatus status = ProjectStatus.PENDING;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
