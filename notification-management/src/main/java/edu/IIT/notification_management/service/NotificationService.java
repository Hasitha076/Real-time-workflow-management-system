package edu.IIT.notification_management.service;

import edu.IIT.project_management.dto.ProjectCreateEventDTO;
import edu.IIT.project_management.dto.ProjectUpdateEventDTO;

import java.util.List;

public interface NotificationService {

    public void sendEmails(ProjectCreateEventDTO projectCreateEventDTO);
    public void sendUpdatedEmails(ProjectUpdateEventDTO projectUpdateEventDTO);
    public void sendMail(List<String> to, String subject, String body);
}
