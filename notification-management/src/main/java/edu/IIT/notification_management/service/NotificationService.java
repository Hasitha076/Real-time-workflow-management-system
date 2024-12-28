package edu.IIT.notification_management.service;

import edu.IIT.notification_management.dto.NotificationDTO;
import edu.IIT.notification_management.dto.NotificationEventDTO;
import edu.IIT.project_management.dto.ProjectCreateEventDTO;
import edu.IIT.project_management.dto.ProjectDeleteEventDTO;
import edu.IIT.project_management.dto.ProjectUpdateEventDTO;
import edu.IIT.task_management.dto.TaskCreateEventDTO;
import edu.IIT.task_management.dto.TaskDeleteEventDTO;
import edu.IIT.task_management.dto.TaskUpdateEventDTO;

import java.util.List;

public interface NotificationService {

//    Project
    public void sendEmails(ProjectCreateEventDTO projectCreateEventDTO);
    public void sendUpdatedEmails(ProjectUpdateEventDTO projectUpdateEventDTO);
    public void sendDeleteEmails(ProjectDeleteEventDTO projectDeleteEventDTO);

//    Task
    public void sendTaskCreateEmails(TaskCreateEventDTO taskCreateEventDTO);
    public void sendTaskUpdatedEmails(TaskUpdateEventDTO taskUpdateEventDTO);
    public void sendTaskDeleteEmails(TaskDeleteEventDTO taskDeleteEventDTO);

    public void sendMail(List<String> to, String subject, String body);

    public void createNotification(TaskCreateEventDTO taskCreateEventDTO, String subject);
    public void updateNotification(TaskUpdateEventDTO taskUpdateEventDTO, String subject);
    public void deleteNotification(TaskDeleteEventDTO taskDeleteEventDTO, String subject);
}
