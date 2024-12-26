package edu.IIT.notification_management.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class NotificationDTO {
    private List<String> recipients;
    private String subject;
    private String body;

}
