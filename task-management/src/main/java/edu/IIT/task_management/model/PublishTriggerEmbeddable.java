package edu.IIT.task_management.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublishTriggerEmbeddable {
    private int id;
    private String name;
    private String type;
    private String status;

    @Embedded
    private PublishTriggerDetails triggerDetails;
}

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class PublishTriggerDetails {
    private String triggerType;
}
