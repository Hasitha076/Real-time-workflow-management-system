package edu.IIT.task_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TaskPriorityLevel {
    @JsonProperty("HIGH")
    HIGH,
    @JsonProperty("MEDIUM")
    MEDIUM,
    @JsonProperty("LOW")
    LOW
}
