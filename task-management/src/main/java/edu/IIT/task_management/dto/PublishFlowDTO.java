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
    private int ruleId;
    private String ruleName;
    private List<TriggerDTO> triggers;
    private List<ActionDTO> actions;
    private int projectId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
