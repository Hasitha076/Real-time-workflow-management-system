package edu.IIT.notification_management.consumer;

import edu.IIT.notification_management.service.NotificationService;
import edu.IIT.project_management.dto.ProjectCreateEventDTO;
import edu.IIT.project_management.dto.ProjectDeleteEventDTO;
import edu.IIT.project_management.dto.ProjectUpdateEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "project-create-events", groupId = "notification-management")
    public void consumeCreateProject(ProjectCreateEventDTO message) {
        try {
            assert message != null;

            notificationService.sendEmails(message);

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

    @KafkaListener(topics = "project-update-events", groupId = "notification-management")
    public void consumeUpdateProject(ProjectUpdateEventDTO message) {
        try {
            assert message != null;

            notificationService.sendUpdatedEmails(message);

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

    @KafkaListener(topics = "project-delete-events", groupId = "notification-management")
    public void consumeDeleteProject(ProjectDeleteEventDTO message) {
        try {
            assert message != null;

            notificationService.sendDeleteEmails(message);

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }
}
