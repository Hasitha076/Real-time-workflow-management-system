package edu.IIT.task_management.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TriggerDTO {
    private String type;
    private Map<String, Object> triggerDetails;
}
