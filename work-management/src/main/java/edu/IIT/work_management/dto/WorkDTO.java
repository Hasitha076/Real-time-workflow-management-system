package edu.IIT.work_management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WorkDTO {
    private int workId;
    private String workName;
    private String description;
    private boolean status;
    private int assignerId;

    private WorkPriorityLevel priority;
    private int projectId;
    private List<Integer> collaboratorIds;
    private List<Integer> teamIds;
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
