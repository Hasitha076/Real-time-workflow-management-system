package edu.IIT.team_management.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TeamDTO {
    private int teamId;
    private String teamName;
    private String teamDescription;
    private int assignerId = 1;
    private List<String> tags;
    private List<Integer> collaboratorIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
