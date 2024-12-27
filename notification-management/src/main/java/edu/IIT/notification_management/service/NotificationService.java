package edu.IIT.notification_management.service;

import edu.IIT.project_management.dto.ProjectCreateEventDTO;
import edu.IIT.project_management.dto.ProjectDeleteEventDTO;
import edu.IIT.project_management.dto.ProjectUpdateEventDTO;
import edu.IIT.task_management.dto.TaskCreateEventDTO;
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
//    public void sendTaskDeleteEmails(ProjectDeleteEventDTO projectDeleteEventDTO);

    public void sendMail(List<String> to, String subject, String body);
}
