package edu.IIT.task_management.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PublishFlowDTO {
    private int publishFlowId;
    private String publishFlowName;
    private List<TriggerDTO> triggers;
    private List<ActionDTO> actions;
    private int projectId;
    private int ruleId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
