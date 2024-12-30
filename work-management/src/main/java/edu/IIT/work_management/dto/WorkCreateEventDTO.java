package edu.IIT.work_management.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WorkCreateEventDTO {
    private String workName;
    private int assignerId;
    private List<Integer> collaboratorIds;
}
