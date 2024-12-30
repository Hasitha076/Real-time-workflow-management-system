package edu.IIT.team_management.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TeamDeleteEventDTO {
    private int teamId;
    private String teamName;
    private int assignerId;
    private List<Integer> collaboratorIds;
}
