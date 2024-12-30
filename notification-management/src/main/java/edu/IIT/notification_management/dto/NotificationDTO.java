package edu.IIT.notification_management.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NotificationDTO {
    private List<String> recipients;
    private String subject;
    private String body;

}
