package edu.IIT.project_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ProjectPriorityLevel {
    @JsonProperty("HIGH")
    HIGH,
    @JsonProperty("MEDIUM")
    MEDIUM,
    @JsonProperty("LOW")
    LOW
}
