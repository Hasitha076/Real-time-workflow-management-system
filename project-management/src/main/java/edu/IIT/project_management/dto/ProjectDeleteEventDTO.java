package edu.IIT.project_management.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProjectDeleteEventDTO {
    private String projectName;
    private int assignerId;
    private List<Integer> collaboratorIds;
}
