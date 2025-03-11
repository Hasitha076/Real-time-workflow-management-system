package edu.IIT.notification_management.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.IIT.notification_management.dto.ProjectCreateEventDTO;
import edu.IIT.notification_management.dto.ProjectDeleteEventDTO;
import edu.IIT.notification_management.dto.ProjectUpdateEventDTO;
import edu.IIT.notification_management.service.NotificationService;
import edu.IIT.task_management.dto.TaskCreateEventDTO;
import edu.IIT.task_management.dto.TaskDeleteEventDTO;
import edu.IIT.task_management.dto.TaskUpdateEventDTO;
import edu.IIT.team_management.dto.TeamCreateEventDTO;
import edu.IIT.team_management.dto.TeamDeleteEventDTO;
import edu.IIT.team_management.dto.TeamUpdateEventDTO;
import edu.IIT.work_management.dto.WorkCreateEventDTO;
import edu.IIT.work_management.dto.WorkDeleteEventDTO;
import edu.IIT.work_management.dto.WorkUpdateEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

//    @KafkaListener(topics = "project-create-events", groupId = "notification-management")
//    public void consumeCreateProject(ProjectCreateEventDTO message) {
//        try {
//            assert message != null;
//
//            notificationService.sendEmails(message);
//            notificationService.createProjectNotification(message, null, "PROJECT");
//
//        } catch (Exception e) {
//            log.error("Error consuming message", e);
//        }
//    }

    @KafkaListener(topics = "project-create-events", groupId = "notification-management")
    public void consumeCreateProject(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ProjectCreateEventDTO projectCreateEventDTO = objectMapper.readValue(message, ProjectCreateEventDTO.class);

            System.out.println("Project create message: " + projectCreateEventDTO.getProjectName());
            System.out.println("Project create message: " + projectCreateEventDTO.getAssignerId());
            System.out.println("Project create message: " + projectCreateEventDTO.getCollaboratorIds());


            notificationService.sendEmails(projectCreateEventDTO);
            notificationService.createProjectNotification(projectCreateEventDTO, null, "PROJECT");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }


//    @KafkaListener(topics = "project-update-events", groupId = "notification-management")
//    public void consumeUpdateProject(ProjectUpdateEventDTO message) {
//        try {
//            assert message != null;
//
//            notificationService.sendUpdatedEmails(message);
//            notificationService.updateProjectNotification(message, null, "PROJECT");
//
//        } catch (Exception e) {
//            log.error("Error consuming message", e);
//        }
//    }

    @KafkaListener(topics = "project-update-events", groupId = "notification-management")
    public void consumeUpdateProject(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ProjectUpdateEventDTO projectUpdateEventDTO = objectMapper.readValue(message, ProjectUpdateEventDTO.class);

            System.out.println("Project create message: " + projectUpdateEventDTO.getProjectName());
            System.out.println("Project create message: " + projectUpdateEventDTO.getAssignerId());
            System.out.println("Project create message: " + projectUpdateEventDTO.getCollaboratorIds());


            notificationService.sendUpdatedEmails(projectUpdateEventDTO);
            notificationService.updateProjectNotification(projectUpdateEventDTO, null, "PROJECT");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

//    @KafkaListener(topics = "project-delete-events", groupId = "notification-management")
//    public void consumeDeleteProject(ProjectDeleteEventDTO message) {
//        try {
//            assert message != null;
//
//            notificationService.sendDeleteEmails(message);
//            notificationService.deleteProjectNotification(message, null, "PROJECT");
//
//        } catch (Exception e) {
//            log.error("Error consuming message", e);
//        }
//    }

    @KafkaListener(topics = "project-delete-events", groupId = "notification-management")
    public void consumeDeleteProject(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ProjectDeleteEventDTO projectDeleteEventDTO = objectMapper.readValue(message, ProjectDeleteEventDTO.class);

            System.out.println("Project create message: " + projectDeleteEventDTO.getProjectName());
            System.out.println("Project create message: " + projectDeleteEventDTO.getAssignerId());
            System.out.println("Project create message: " + projectDeleteEventDTO.getCollaboratorIds());


            notificationService.sendDeleteEmails(projectDeleteEventDTO);
            notificationService.deleteProjectNotification(projectDeleteEventDTO, null, "PROJECT");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

    @KafkaListener(topics = "task-create-events", groupId = "notification-management")
    public void consumeCreateTask(TaskCreateEventDTO message) {
        try {
            assert message != null;

            notificationService.sendTaskCreateEmails(message);
            notificationService.createTaskNotification(message, null, "task");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

    @KafkaListener(topics = "task-update-events", groupId = "notification-management")
    public void consumeUpdateTask(TaskUpdateEventDTO message) {
        try {
            assert message != null;

            notificationService.sendTaskUpdatedEmails(message);
            notificationService.updateTaskNotification(message, null, "task");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

    @KafkaListener(topics = "task-delete-events", groupId = "notification-management")
    public void consumeDeleteTask(TaskDeleteEventDTO message) {
        try {
            assert message != null;

            notificationService.sendTaskDeleteEmails(message);
            notificationService.deleteTaskNotification(message, null, "task");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }


//    Team
    @KafkaListener(topics = "team-create-events", groupId = "notification-management")
    public void consumeCreateTeam(TeamCreateEventDTO message) {
        try {
            assert message != null;

            notificationService.sendTeamCreateEmails(message);
            notificationService.createTeamNotification(message, null, "team");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

    @KafkaListener(topics = "team-update-events", groupId = "notification-management")
    public void consumeUpdateTeam(TeamUpdateEventDTO message) {
        try {
            assert message != null;

            notificationService.sendTeamUpdatedEmails(message);
            notificationService.updateTeamNotification(message, null, "team");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

    @KafkaListener(topics = "team-delete-events", groupId = "notification-management")
    public void consumeDeleteTeam(TeamDeleteEventDTO message) {
        try {
            assert message != null;

            notificationService.sendTeamDeleteEmails(message);
            notificationService.deleteTeamNotification(message, null, "team");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }


    //    Work
    @KafkaListener(topics = "work-create-events", groupId = "notification-management")
    public void consumeCreateWork(WorkCreateEventDTO message) {
        try {
            assert message != null;

//            notificationService.sendWorkCreateEmails(message);
//            notificationService.createWorkNotification(message, null, "team");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

    @KafkaListener(topics = "work-update-events", groupId = "notification-management")
    public void consumeUpdateWork(WorkUpdateEventDTO message) {
        try {
            assert message != null;

            notificationService.sendWorkUpdatedEmails(message);
            notificationService.updateWorkNotification(message, null, "work");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

    @KafkaListener(topics = "work-delete-events", groupId = "notification-management")
    public void consumeDeleteWork(WorkDeleteEventDTO message) {
        try {
            assert message != null;

            notificationService.sendWorkDeleteEmails(message);
            notificationService.deleteWorkNotification(message, null, "work");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }
}
