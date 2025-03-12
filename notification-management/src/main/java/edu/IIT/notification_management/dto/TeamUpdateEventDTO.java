package edu.IIT.notification_management.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TeamUpdateEventDTO {
    private String teamName;
    private int assignerId;
    private String collaboratorAssignmentType;
    private List<Integer> collaboratorIds;
}
