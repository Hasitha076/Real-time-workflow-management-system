package edu.IIT.notification_management.consumer;

import edu.IIT.notification_management.service.NotificationService;
import edu.IIT.project_management.dto.ProjectDTO;
import edu.IIT.project_management.dto.ProjectEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "project-events", groupId = "notification-management")
    public void consumeProject(ProjectEventDTO message) {
        try {
            assert message != null;

            notificationService.sendEmails(message);

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }
}
