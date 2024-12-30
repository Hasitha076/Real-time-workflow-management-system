package edu.IIT.notification_management.model;

import edu.IIT.notification_management.dto.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int notificationId;
    private String notificationName;
    private NotificationType notificationType;
    private int assignerId;

    @ElementCollection
    @CollectionTable(name = "notification_collaborators", joinColumns = @JoinColumn(name = "notification_id"))
    @Column(name = "collaborator_id")
    private List<Integer> collaboratorIds;

    private String subject;
}
