package edu.IIT.task_management.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TaskUpdateEventDTO {
    private String taskName;
    private String collaboratorAssignmentType;
    private List<Integer> collaboratorIds;
}
