package edu.IIT.task_management.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TaskCreateEventDTO {
    private String TaskName;
    private int assignerId;
    private List<Integer> collaboratorIds;
}
