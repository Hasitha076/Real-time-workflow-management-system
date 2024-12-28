package edu.IIT.notification_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class NotificationEventDTO {
    private int notificationId;
    private String taskName;
    private int assignerId;
    private List<Integer> collaboratorIds;
    private String subject;
}
