package edu.IIT.notification_management.model;

import edu.IIT.notification_management.dto.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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
    private String body;

    @ElementCollection
    @CollectionTable(name = "notification_collaborators", joinColumns = @JoinColumn(name = "notification_id"))
    @Column(name = "collaborator_id")
    private List<Integer> collaboratorIds;

    private String subject;

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
}
