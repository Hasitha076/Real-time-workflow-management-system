package edu.IIT.task_management.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollaboratorsBlockDTO {
    private int collaboratorsBlockId;
    private int projectId;
    private int workId;
    private List<Integer> memberIds;
    private List<Integer> teamIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
