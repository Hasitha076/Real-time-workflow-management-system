package edu.IIT.project_management.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class ProjectDTO {
    private int projectId;
    private String projectName;
    private ProjectPriorityLevel priority;
    private List<Integer> collaboratorIds;
    private ProjectStatus status = ProjectStatus.PENDING;
    private String createdAt;
    private String updatedAt;

}
