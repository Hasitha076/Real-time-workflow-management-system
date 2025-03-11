package edu.IIT.notification_management.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProjectCreateEventDTO {
    private String projectName;
    private int assignerId;
    private List<Integer> collaboratorIds;
}
