package edu.IIT.notification_management.controller;

import edu.IIT.notification_management.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
@CrossOrigin
public class NotificationController {

    private final NotificationService notificationService;

}
