package edu.IIT.project_management.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CollaboratorsRequest {
    private List<Integer> collaboratorIds;
    private List<Integer> teamIds;
}
