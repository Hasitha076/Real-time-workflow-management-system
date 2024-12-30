package edu.IIT.work_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum WorkPriorityLevel {
    @JsonProperty("HIGH")
    HIGH,
    @JsonProperty("MEDIUM")
    MEDIUM,
    @JsonProperty("LOW")
    LOW
}
