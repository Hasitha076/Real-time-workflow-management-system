package edu.IIT.project_management.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProjectUpdateEventDTO {
    private String projectName;
    private String collaboratorAssignmentType;
    private List<Integer> collaboratorIds;
}
