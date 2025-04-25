package edu.IIT.notification_management.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.IIT.notification_management.dto.*;
import edu.IIT.notification_management.service.NotificationService;
import edu.IIT.user_management.dto.OTPRequest;
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
    public void consumeCreateTask(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TaskCreateEventDTO taskCreateEventDTO = objectMapper.readValue(message, TaskCreateEventDTO.class);

            System.out.println("Task create message: " + taskCreateEventDTO.getTaskName());
            System.out.println("Task create message: " + taskCreateEventDTO.getAssignerId());
            System.out.println("Task create message: " + taskCreateEventDTO.getCollaboratorIds());


            notificationService.sendTaskCreateEmails(taskCreateEventDTO);
            notificationService.createTaskNotification(taskCreateEventDTO, null, "task");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }


    @KafkaListener(topics = "task-update-events", groupId = "notification-management")
    public void consumeUpdateTask(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TaskUpdateEventDTO taskUpdateEventDTO = objectMapper.readValue(message, TaskUpdateEventDTO.class);

            System.out.println("Task update message: " + taskUpdateEventDTO.getTaskName());
            System.out.println("Task update message: " + taskUpdateEventDTO.getAssignerId());
            System.out.println("Task update message: " + taskUpdateEventDTO.getCollaboratorIds());


            notificationService.sendTaskUpdatedEmails(taskUpdateEventDTO);
            notificationService.updateTaskNotification(taskUpdateEventDTO, null, "task");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

    @KafkaListener(topics = "task-status-events", groupId = "notification-management")
    public void consumeUpdateStatusTask(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TaskUpdateEventDTO taskUpdateEventDTO = objectMapper.readValue(message, TaskUpdateEventDTO.class);

            System.out.println("Task update message: " + taskUpdateEventDTO.getTaskName());
            System.out.println("Task update message: " + taskUpdateEventDTO.getAssignerId());
            System.out.println("Task update message: " + taskUpdateEventDTO.getCollaboratorIds());

            notificationService.sendTaskStatusUpdatedEmails(taskUpdateEventDTO);
            notificationService.updateTaskStatusNotification(taskUpdateEventDTO);

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

    @KafkaListener(topics = "task-delete-events", groupId = "notification-management")
    public void consumeDeleteTask(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TaskDeleteEventDTO taskDeleteEventDTO = objectMapper.readValue(message, TaskDeleteEventDTO.class);

            System.out.println("Task delete message: " + taskDeleteEventDTO.getTaskName());
            System.out.println("Task delete message: " + taskDeleteEventDTO.getAssignerId());
            System.out.println("Task delete message: " + taskDeleteEventDTO.getCollaboratorIds());


            notificationService.sendTaskDeleteEmails(taskDeleteEventDTO);
            notificationService.deleteTaskNotification(taskDeleteEventDTO, null, "task");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

    @KafkaListener(topics = "team-create-events", groupId = "notification-management")
    public void consumeCreateTeam(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TeamCreateEventDTO teamCreateEventDTO = objectMapper.readValue(message, TeamCreateEventDTO.class);

            System.out.println("Team create message: " + teamCreateEventDTO.getTeamName());
            System.out.println("Team create message: " + teamCreateEventDTO.getAssignerId());
            System.out.println("Team create message: " + teamCreateEventDTO.getCollaboratorIds());


            notificationService.sendTeamCreateEmails(teamCreateEventDTO);
            notificationService.createTeamNotification(teamCreateEventDTO, null, "team");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

    @KafkaListener(topics = "team-update-events", groupId = "notification-management")
    public void consumeUpdateTeam(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TeamUpdateEventDTO teamUpdateEventDTO = objectMapper.readValue(message, TeamUpdateEventDTO.class);

            System.out.println("Team update message: " + teamUpdateEventDTO.getTeamName());
            System.out.println("Team update message: " + teamUpdateEventDTO.getAssignerId());
            System.out.println("Team update message: " + teamUpdateEventDTO.getCollaboratorIds());


            notificationService.sendTeamUpdatedEmails(teamUpdateEventDTO);
            notificationService.updateTeamNotification(teamUpdateEventDTO, null, "team");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

    @KafkaListener(topics = "team-delete-events", groupId = "notification-management")
    public void consumeDeleteTeam(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TeamDeleteEventDTO teamDeleteEventDTO = objectMapper.readValue(message, TeamDeleteEventDTO.class);

            System.out.println("Team delete message: " + teamDeleteEventDTO.getTeamName());
            System.out.println("Team delete message: " + teamDeleteEventDTO.getAssignerId());
            System.out.println("Team delete message: " + teamDeleteEventDTO.getCollaboratorIds());


            notificationService.sendTeamDeleteEmails(teamDeleteEventDTO);
            notificationService.deleteTeamNotification(teamDeleteEventDTO, null, "team");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

    @KafkaListener(topics = "work-create-events", groupId = "notification-management")
    public void consumeCreateWork(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            WorkCreateEventDTO workCreateEventDTO = objectMapper.readValue(message, WorkCreateEventDTO.class);

            System.out.println("Work create message: " + workCreateEventDTO.getWorkName());
            System.out.println("Work create message: " + workCreateEventDTO.getAssignerId());
            System.out.println("Work create message: " + workCreateEventDTO.getCollaboratorIds());


            notificationService.sendWorkCreateEmails(workCreateEventDTO);
            notificationService.createWorkNotification(workCreateEventDTO, null, "work");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

    @KafkaListener(topics = "work-update-events", groupId = "notification-management")
    public void consumeUpdateWork(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            WorkUpdateEventDTO workUpdateEventDTO = objectMapper.readValue(message, WorkUpdateEventDTO.class);

            System.out.println("Work update message: " + workUpdateEventDTO.getWorkName());
            System.out.println("Work update message: " + workUpdateEventDTO.getAssignerId());
            System.out.println("Work update message: " + workUpdateEventDTO.getCollaboratorIds());


            notificationService.sendWorkUpdatedEmails(workUpdateEventDTO);
            notificationService.updateWorkNotification(workUpdateEventDTO, null, "work");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

    @KafkaListener(topics = "work-delete-events", groupId = "notification-management")
    public void consumeDeleteWork(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            WorkDeleteEventDTO workDeleteEventDTO = objectMapper.readValue(message, WorkDeleteEventDTO.class);

            System.out.println("Work delete message: " + workDeleteEventDTO.getWorkName());
            System.out.println("Work delete message: " + workDeleteEventDTO.getAssignerId());
            System.out.println("Work delete message: " + workDeleteEventDTO.getCollaboratorIds());


            notificationService.sendWorkDeleteEmails(workDeleteEventDTO);
            notificationService.deleteWorkNotification(workDeleteEventDTO, null, "work");

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }


    @KafkaListener(topics = "OTP-events", groupId = "notification-management")
    public void consumeOTP(OTPRequest otpRequest) {
        try {
            System.out.println("email: " + otpRequest.getEmail());
            System.out.println("OTP: " + otpRequest.getOTP());
            notificationService.sendOTP(otpRequest);

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }
}
