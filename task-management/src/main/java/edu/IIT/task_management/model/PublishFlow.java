package edu.IIT.task_management.model;

import com.fasterxml.jackson.core.type.TypeReference;
import edu.IIT.task_management.dto.ActionDTO;
import edu.IIT.task_management.dto.TriggerDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "publish_flow")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublishFlow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ruleId;

    private String ruleName;
    private int projectId;

    @Lob
    private String triggersJson;

    @Lob
    private String actionsJson;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Convenience methods
    public void setTriggers(List<TriggerDTO> triggers) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        this.triggersJson = mapper.writeValueAsString(triggers);
    }

    public List<TriggerDTO> getTriggers() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(this.triggersJson, new TypeReference<List<TriggerDTO>>() {});
    }

    public void setActions(List<ActionDTO> actions) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        this.actionsJson = mapper.writeValueAsString(actions);
    }

    public List<ActionDTO> getActions() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(this.actionsJson, new TypeReference<List<ActionDTO>>() {});
    }
}

