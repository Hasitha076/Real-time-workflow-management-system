package edu.IIT.notification_management.service;

import edu.IIT.notification_management.dto.NotificationDTO;
import edu.IIT.project_management.dto.ProjectEventDTO;

import java.util.List;

public interface NotificationService {

    public void sendEmails(ProjectEventDTO projectEventDTO);
}
