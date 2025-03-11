package edu.IIT.notification_management.service;

import edu.IIT.notification_management.dto.ProjectCreateEventDTO;
import edu.IIT.notification_management.dto.ProjectDeleteEventDTO;
import edu.IIT.notification_management.dto.ProjectUpdateEventDTO;
import edu.IIT.task_management.dto.TaskCreateEventDTO;
import edu.IIT.task_management.dto.TaskDeleteEventDTO;
import edu.IIT.task_management.dto.TaskUpdateEventDTO;
import edu.IIT.team_management.dto.TeamCreateEventDTO;
import edu.IIT.team_management.dto.TeamDeleteEventDTO;
import edu.IIT.team_management.dto.TeamUpdateEventDTO;
import edu.IIT.work_management.dto.WorkCreateEventDTO;
import edu.IIT.work_management.dto.WorkDeleteEventDTO;
import edu.IIT.work_management.dto.WorkUpdateEventDTO;

import java.util.List;

public interface NotificationService {

//    Project Email Notification
    public void sendEmails(ProjectCreateEventDTO projectCreateEventDTO);
    public void sendUpdatedEmails(ProjectUpdateEventDTO projectUpdateEventDTO);
    public void sendDeleteEmails(ProjectDeleteEventDTO projectDeleteEventDTO);

//    Task Email Notification
    public void sendTaskCreateEmails(TaskCreateEventDTO taskCreateEventDTO);
    public void sendTaskUpdatedEmails(TaskUpdateEventDTO taskUpdateEventDTO);
    public void sendTaskDeleteEmails(TaskDeleteEventDTO taskDeleteEventDTO);

//    Team Email Notification
    public void sendTeamCreateEmails(TeamCreateEventDTO teamCreateEventDTO);
    public void sendTeamUpdatedEmails(TeamUpdateEventDTO teamUpdateEventDTO);
    public void sendTeamDeleteEmails(TeamDeleteEventDTO teamDeleteEventDTO);

//    Work Email Notification
//    public void sendWorkCreateEmails(WorkCreateEventDTO workCreateEventDTO);
    public void sendWorkUpdatedEmails(WorkUpdateEventDTO workUpdateEventDTO);
    public void sendWorkDeleteEmails(WorkDeleteEventDTO workDeleteEventDTO);

    public void sendMail(List<String> to, String subject, String body);

//    Task Inbox Notification
    public void createTaskNotification(TaskCreateEventDTO taskCreateEventDTO, String subject, String type);
    public void updateTaskNotification(TaskUpdateEventDTO taskUpdateEventDTO, String subject, String type);
    public void deleteTaskNotification(TaskDeleteEventDTO taskDeleteEventDTO, String subject, String type);

//    Project Inbox Notification
    public void createProjectNotification(ProjectCreateEventDTO projectCreateEventDTO, String subject, String type);
    public void updateProjectNotification(ProjectUpdateEventDTO projectUpdateEventDTO, String subject, String type);
    public void deleteProjectNotification(ProjectDeleteEventDTO projectDeleteEventDTO, String subject, String type);

//    Team Inbox Notification
    public void createTeamNotification(TeamCreateEventDTO teamCreateEventDTO, String subject, String type);
    public void updateTeamNotification(TeamUpdateEventDTO teamUpdateEventDTO, String subject, String type);
    public void deleteTeamNotification(TeamDeleteEventDTO teamDeleteEventDTO, String subject, String type);

//    Work Inbox Notification
//    public void createWorkNotification(WorkCreateEventDTO workCreateEventDTO, String subject, String type);
    public void updateWorkNotification(WorkUpdateEventDTO workUpdateEventDTO, String subject, String type);
    public void deleteWorkNotification(WorkDeleteEventDTO workDeleteEventDTO, String subject, String type);
}
