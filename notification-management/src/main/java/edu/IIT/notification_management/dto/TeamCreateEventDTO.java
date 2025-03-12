package edu.IIT.notification_management.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TeamCreateEventDTO {
    private String teamName;
    private int assignerId;
    private List<Integer> collaboratorIds;
}
