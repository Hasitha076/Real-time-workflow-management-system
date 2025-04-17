package edu.IIT.notification_management.controller;

import edu.IIT.notification_management.dto.NotificationEventDTO;
import edu.IIT.notification_management.service.NotificationService;
import edu.IIT.notification_management.service.NotificationServiceQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationServiceQuery notificationServiceQuery;

    @GetMapping("/getAllNotifications")
    public List<NotificationEventDTO> getAllNotifications() {
       return notificationServiceQuery.getAllNotifications();
    }

    @GetMapping("/getNotificationById/{id}")
    public NotificationEventDTO getNotificationById(@PathVariable int id) {
        return notificationServiceQuery.getNotificationById(id);
    }

}
