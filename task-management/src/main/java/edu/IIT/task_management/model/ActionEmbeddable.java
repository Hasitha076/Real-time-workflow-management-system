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
public class ActionEmbeddable {
    private String type;

    @Embedded
    private ActionDetails actionDetails;
}

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ActionDetails {
    private String actionType;
    private Assignee assignee;
}

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class Assignee {
    private int id;
    private String name;
    private String email;

}
