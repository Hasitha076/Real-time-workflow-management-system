package edu.IIT.notification_management.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NotificationEventDTO {
    private int notificationId;
    private String notificationName;
    private NotificationType notificationType;
    private int assignerId;
    private List<Integer> collaboratorIds;
    private String subject;
    private String body;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
