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
public class PublishActionEmbeddable {
    private String type;

    @Embedded
    private PublishActionDetails actionDetails;
}

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class PublishActionDetails {
    private String actionType;
    private PublishAssignee assignee;
}

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class PublishAssignee {
    private int id;
    private String name;
    private String email;

}
