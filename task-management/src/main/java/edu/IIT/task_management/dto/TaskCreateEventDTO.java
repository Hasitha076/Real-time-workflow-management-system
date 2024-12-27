package edu.IIT.task_management.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TaskCreateEventDTO {
    private String taskName;
    private List<Integer> collaboratorIds;
}
