package edu.IIT.work_management.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WorkUpdateEventDTO {
    private String workName;
    private int assignerId;
    private String collaboratorAssignmentType;
    private List<Integer> collaboratorIds;
}
