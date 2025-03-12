package edu.IIT.notification_management.service;

import edu.IIT.notification_management.dto.*;
import edu.IIT.notification_management.model.Notification;
import edu.IIT.notification_management.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final ModelMapper modelMapper;
    private final WebClient userWebClient;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private NotificationRepository notificationRepository;

    NotificationServiceImpl(WebClient userWebClient, ModelMapper modelMapper) {
        this.userWebClient = userWebClient;
        this.modelMapper = modelMapper;
    }

    @Override
    public void sendEmails(ProjectCreateEventDTO projectCreateEventDTO) {
        List<Integer> ids = projectCreateEventDTO.getCollaboratorIds();

        List<String> recipients = userWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterUsers") // Ensure path matches the controller
                        .queryParam("ids", ids) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        log.info("#### -> Sending email -> {}", recipients);

        String subject = "New Project Created: " + projectCreateEventDTO.getProjectName();
        String body = "You have been added as a collaborator to the project: " + projectCreateEventDTO.getProjectName();

        assert recipients != null;
        sendMail(recipients, subject, body);
    }

    @Override
    public void sendUpdatedEmails(ProjectUpdateEventDTO projectUpdateEventDTO) {
        List<Integer> ids = projectUpdateEventDTO.getCollaboratorIds();

        List<String> recipients = userWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterUsers") // Ensure path matches the controller
                        .queryParam("ids", ids) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        log.info("#### -> Sending email -> {}", recipients);
        String subject = null;
        String body = null;
        if(projectUpdateEventDTO.getCollaboratorAssignmentType().equals("New")) {
            subject = "New Project Created: " + projectUpdateEventDTO.getProjectName();
            body = "You have been added as a collaborator to the project: " + projectUpdateEventDTO.getProjectName();
        } else if(projectUpdateEventDTO.getCollaboratorAssignmentType().equals("Removed")) {
            subject = "Collaborators Removed from Project: " + projectUpdateEventDTO.getProjectName();
            body = "You have been removed from the project: " + projectUpdateEventDTO.getProjectName();
        } else if(projectUpdateEventDTO.getCollaboratorAssignmentType().equals("Existing")) {
            subject = "Project has been changed as: " + projectUpdateEventDTO.getProjectName();
            body = "Project has been changed as: " + projectUpdateEventDTO.getProjectName();
        }

        assert recipients != null;
        sendMail(recipients, subject, body);
    }

    @Override
    public void sendDeleteEmails(ProjectDeleteEventDTO projectDeleteEventDTO) {
        List<Integer> ids = projectDeleteEventDTO.getCollaboratorIds();

        List<String> recipients = userWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterUsers") // Ensure path matches the controller
                        .queryParam("ids", ids) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        log.info("#### -> Sending email -> {}", recipients);

        String subject = projectDeleteEventDTO.getProjectName() + " project is deleted";
        String body = projectDeleteEventDTO.getProjectName() + " project is already deleted";

        assert recipients != null;
        sendMail(recipients, subject, body);
    }


//    Task
@Override
public void sendTaskCreateEmails(TaskCreateEventDTO taskCreateEventDTO) {
    List<Integer> ids = taskCreateEventDTO.getCollaboratorIds();

    List<String> recipients = userWebClient.get()
            .uri(uriBuilder -> uriBuilder.path("/filterUsers") // Ensure path matches the controller
                    .queryParam("ids", ids) // Ensure param name matches the controller
                    .build())
            .retrieve()
            .bodyToMono(List.class)
            .block();

    log.info("#### -> Sending email -> {}", recipients);

    String subject = "New Task Created: " + taskCreateEventDTO.getTaskName();
    String body = "You have been added as a collaborator to the task: " + taskCreateEventDTO.getTaskName();

    assert recipients != null;
    sendMail(recipients, subject, body);
}

    @Override
    public void sendTaskUpdatedEmails(TaskUpdateEventDTO taskUpdateEventDTO) {
        List<Integer> ids = taskUpdateEventDTO.getCollaboratorIds();

        List<String> recipients = userWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterUsers") // Ensure path matches the controller
                        .queryParam("ids", ids) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        log.info("#### -> Sending email -> {}", recipients);
        String subject = null;
        String body = null;
        if(taskUpdateEventDTO.getCollaboratorAssignmentType().equals("New")) {
            subject = "New Task Created: " + taskUpdateEventDTO.getTaskName();
            body = "You have been added as a collaborator to the task: " + taskUpdateEventDTO.getTaskName();
        } else if(taskUpdateEventDTO.getCollaboratorAssignmentType().equals("Removed")) {
            subject = "Collaborators Removed from Task: " + taskUpdateEventDTO.getTaskName();
            body = "You have been removed from the task: " + taskUpdateEventDTO.getTaskName();
        } else if(taskUpdateEventDTO.getCollaboratorAssignmentType().equals("Existing")) {
            subject = "Task has been changed as: " + taskUpdateEventDTO.getTaskName();
            body = "Task has been changed as: " + taskUpdateEventDTO.getTaskName();
        }

        assert recipients != null;
        sendMail(recipients, subject, body);
    }

    @Override
    public void sendTaskDeleteEmails(TaskDeleteEventDTO taskDeleteEventDTO) {
        List<Integer> ids = taskDeleteEventDTO.getCollaboratorIds();

        List<String> recipients = userWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterUsers") // Ensure path matches the controller
                        .queryParam("ids", ids) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        log.info("#### -> Sending email -> {}", recipients);

        String subject = taskDeleteEventDTO.getTaskName() + " task is deleted";
        String body = taskDeleteEventDTO.getTaskName() + " task is already deleted";

        assert recipients != null;
        sendMail(recipients, subject, body);
    }


    //    Team
    @Override
    public void sendTeamCreateEmails(TeamCreateEventDTO teamCreateEventDTO) {
        List<Integer> ids = teamCreateEventDTO.getCollaboratorIds();

        List<String> recipients = userWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterUsers") // Ensure path matches the controller
                        .queryParam("ids", ids) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        log.info("#### -> Sending email -> {}", recipients);

        String subject = "New Team Created: " + teamCreateEventDTO.getTeamName();
        String body = "You have been added as a collaborator to the team: " + teamCreateEventDTO.getTeamName();

        assert recipients != null;
        sendMail(recipients, subject, body);
    }

    @Override
    public void sendTeamUpdatedEmails(TeamUpdateEventDTO teamUpdateEventDTO) {
        List<Integer> ids = teamUpdateEventDTO.getCollaboratorIds();

        List<String> recipients = userWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterUsers") // Ensure path matches the controller
                        .queryParam("ids", ids) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        log.info("#### -> Sending email -> {}", recipients);
        String subject = null;
        String body = null;
        if(teamUpdateEventDTO.getCollaboratorAssignmentType().equals("New")) {
            subject = "New Team Created: " + teamUpdateEventDTO.getTeamName();
            body = "You have been added as a collaborator to the team: " + teamUpdateEventDTO.getTeamName();
        } else if(teamUpdateEventDTO.getCollaboratorAssignmentType().equals("Removed")) {
            subject = "Collaborators Removed from Team: " + teamUpdateEventDTO.getTeamName();
            body = "You have been removed from the team: " + teamUpdateEventDTO.getTeamName();
        } else if(teamUpdateEventDTO.getCollaboratorAssignmentType().equals("Existing")) {
            subject = "Team has been changed as: " + teamUpdateEventDTO.getTeamName();
            body = "Team has been changed as: " + teamUpdateEventDTO.getTeamName();
        }

        assert recipients != null;
        sendMail(recipients, subject, body);
    }

    @Override
    public void sendTeamDeleteEmails(TeamDeleteEventDTO teamDeleteEventDTO) {
        List<Integer> ids = teamDeleteEventDTO.getCollaboratorIds();

        List<String> recipients = userWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterUsers") // Ensure path matches the controller
                        .queryParam("ids", ids) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        log.info("#### -> Sending email -> {}", recipients);

        String subject = teamDeleteEventDTO.getTeamName() + " team is deleted";
        String body = teamDeleteEventDTO.getTeamName() + " team is already deleted";

        assert recipients != null;
        sendMail(recipients, subject, body);
    }


    //    Work
    @Override
    public void sendWorkCreateEmails(WorkCreateEventDTO workCreateEventDTO) {
        List<Integer> ids = workCreateEventDTO.getCollaboratorIds();

        List<String> recipients = userWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterUsers") // Ensure path matches the controller
                        .queryParam("ids", ids) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        log.info("#### -> Sending email -> {}", recipients);

        String subject = "New Work Created: " + workCreateEventDTO.getWorkName();
        String body = "You have been added as a collaborator to the work: " + workCreateEventDTO.getWorkName();

        assert recipients != null;
        sendMail(recipients, subject, body);
    }

    @Override
    public void sendWorkUpdatedEmails(WorkUpdateEventDTO workUpdateEventDTO) {
        List<Integer> ids = workUpdateEventDTO.getCollaboratorIds();

        List<String> recipients = userWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterUsers") // Ensure path matches the controller
                        .queryParam("ids", ids) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        log.info("#### -> Sending email -> {}", recipients);
        String subject = null;
        String body = null;
        if(workUpdateEventDTO.getCollaboratorAssignmentType().equals("New")) {
            subject = "New Work Created: " + workUpdateEventDTO.getWorkName();
            body = "You have been added as a collaborator to the work: " + workUpdateEventDTO.getWorkName();
        } else if(workUpdateEventDTO.getCollaboratorAssignmentType().equals("Removed")) {
            subject = "Collaborators Removed from Work: " + workUpdateEventDTO.getWorkName();
            body = "You have been removed from the work: " + workUpdateEventDTO.getWorkName();
        } else if(workUpdateEventDTO.getCollaboratorAssignmentType().equals("Existing")) {
            subject = "Work has been changed as: " + workUpdateEventDTO.getWorkName();
            body = "Work has been changed as: " + workUpdateEventDTO.getWorkName();
        }

        assert recipients != null;
        sendMail(recipients, subject, body);
    }

    @Override
    public void sendWorkDeleteEmails(WorkDeleteEventDTO workDeleteEventDTO) {
        List<Integer> ids = workDeleteEventDTO.getCollaboratorIds();

        List<String> recipients = userWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterUsers") // Ensure path matches the controller
                        .queryParam("ids", ids) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        log.info("#### -> Sending email -> {}", recipients);

        String subject = workDeleteEventDTO.getWorkName() + " work is deleted";
        String body = workDeleteEventDTO.getWorkName() + " work is already deleted";

        assert recipients != null;
        sendMail(recipients, subject, body);
    }


    @Override
    public void sendMail(List<String> to, String subject, String body) {
        to.forEach(recipient -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
        });
    }


//    Task Inbox Notification
    @Override
    public void createTaskNotification(TaskCreateEventDTO taskCreateEventDTO, String subject, String type) {
        System.out.println("#### -> Creating notification -> " + taskCreateEventDTO);
        subject = subject == null ? "task-created" : subject;
        NotificationEventDTO notificationEventDTO = new NotificationEventDTO();
        notificationEventDTO.setNotificationName(taskCreateEventDTO.getTaskName());
        notificationEventDTO.setAssignerId(taskCreateEventDTO.getAssignerId());
        notificationEventDTO.setCollaboratorIds(taskCreateEventDTO.getCollaboratorIds());
        notificationEventDTO.setSubject(subject);
        notificationEventDTO.setNotificationType(NotificationType.TASK);
        notificationRepository.save(modelMapper.map(notificationEventDTO, Notification.class));
    }

    @Override
    public void updateTaskNotification(TaskUpdateEventDTO taskUpdateEventDTO, String subject, String type) {
        System.out.println("#### -> Creating notification -> " + taskUpdateEventDTO);
        if(taskUpdateEventDTO.getCollaboratorAssignmentType().equals("New")) {
            subject = subject == null ? "task-created" : subject;
        } else if(taskUpdateEventDTO.getCollaboratorAssignmentType().equals("Removed")) {
            subject = subject == null ? "removed-from-task" : subject;
        } else if(taskUpdateEventDTO.getCollaboratorAssignmentType().equals("Existing")) {
            subject = subject == null ? "task-changed" : subject;
        }

        NotificationEventDTO notificationEventDTO = new NotificationEventDTO();
        notificationEventDTO.setNotificationName(taskUpdateEventDTO.getTaskName());
        notificationEventDTO.setAssignerId(taskUpdateEventDTO.getAssignerId());
        notificationEventDTO.setCollaboratorIds(taskUpdateEventDTO.getCollaboratorIds());
        notificationEventDTO.setSubject(subject);
        notificationEventDTO.setNotificationType(NotificationType.TASK);
        notificationRepository.save(modelMapper.map(notificationEventDTO, Notification.class));
    }

    @Override
    public void deleteTaskNotification(TaskDeleteEventDTO taskDeleteEventDTO, String subject, String type) {
        System.out.println("#### -> Creating notification -> " + taskDeleteEventDTO);
        subject = subject == null ? "task-removed" : subject;
        NotificationEventDTO notificationEventDTO = new NotificationEventDTO();
        notificationEventDTO.setNotificationName(taskDeleteEventDTO.getTaskName());
        notificationEventDTO.setAssignerId(taskDeleteEventDTO.getAssignerId());
        notificationEventDTO.setCollaboratorIds(taskDeleteEventDTO.getCollaboratorIds());
        notificationEventDTO.setSubject(subject);
        notificationEventDTO.setNotificationType(NotificationType.TASK);
        notificationRepository.save(modelMapper.map(notificationEventDTO, Notification.class));
    }


    //    Project Inbox Notification
    @Override
    public void createProjectNotification(ProjectCreateEventDTO projectCreateEventDTO, String subject, String type) {
        System.out.println("#### -> Creating notification -> " + projectCreateEventDTO);
        subject = subject == null ? "project-created" : subject;
        NotificationEventDTO notificationEventDTO = new NotificationEventDTO();
        notificationEventDTO.setNotificationName(projectCreateEventDTO.getProjectName());
        notificationEventDTO.setAssignerId(projectCreateEventDTO.getAssignerId());
        notificationEventDTO.setCollaboratorIds(projectCreateEventDTO.getCollaboratorIds());
        notificationEventDTO.setSubject(subject);
        notificationEventDTO.setNotificationType(NotificationType.PROJECT);
        notificationRepository.save(modelMapper.map(notificationEventDTO, Notification.class));
    }

    @Override
    public void updateProjectNotification(ProjectUpdateEventDTO projectUpdateEventDTO, String subject, String type) {
        System.out.println("#### -> Creating notification -> " + projectUpdateEventDTO);
        if(projectUpdateEventDTO.getCollaboratorAssignmentType().equals("New")) {
            subject = subject == null ? "project-created" : subject;
        } else if(projectUpdateEventDTO.getCollaboratorAssignmentType().equals("Removed")) {
            subject = subject == null ? "removed-from-project" : subject;
        } else if(projectUpdateEventDTO.getCollaboratorAssignmentType().equals("Existing")) {
            subject = subject == null ? "project-changed" : subject;
        }

        NotificationEventDTO notificationEventDTO = new NotificationEventDTO();
        notificationEventDTO.setNotificationName(projectUpdateEventDTO.getProjectName());
        notificationEventDTO.setAssignerId(projectUpdateEventDTO.getAssignerId());
        notificationEventDTO.setCollaboratorIds(projectUpdateEventDTO.getCollaboratorIds());
        notificationEventDTO.setSubject(subject);
        notificationEventDTO.setNotificationType(NotificationType.PROJECT);
        notificationRepository.save(modelMapper.map(notificationEventDTO, Notification.class));
    }

    @Override
    public void deleteProjectNotification(ProjectDeleteEventDTO projectDeleteEventDTO, String subject, String type) {
        System.out.println("#### -> Creating notification -> " + projectDeleteEventDTO);
        subject = subject == null ? "project-removed" : subject;
        NotificationEventDTO notificationEventDTO = new NotificationEventDTO();
        notificationEventDTO.setNotificationName(projectDeleteEventDTO.getProjectName());
        notificationEventDTO.setAssignerId(projectDeleteEventDTO.getAssignerId());
        notificationEventDTO.setCollaboratorIds(projectDeleteEventDTO.getCollaboratorIds());
        notificationEventDTO.setSubject(subject);
        notificationEventDTO.setNotificationType(NotificationType.PROJECT);
        notificationRepository.save(modelMapper.map(notificationEventDTO, Notification.class));
    }


    //    Team Inbox Notification
    @Override
    public void createTeamNotification(TeamCreateEventDTO teamCreateEventDTO, String subject, String type) {
        System.out.println("#### -> Creating notification to db -> " + teamCreateEventDTO);
        subject = subject == null ? "team-created" : subject;
        NotificationEventDTO notificationEventDTO = new NotificationEventDTO();
        notificationEventDTO.setNotificationName(teamCreateEventDTO.getTeamName());
        notificationEventDTO.setAssignerId(teamCreateEventDTO.getAssignerId());
        notificationEventDTO.setCollaboratorIds(teamCreateEventDTO.getCollaboratorIds());
        notificationEventDTO.setSubject(subject);
        notificationEventDTO.setNotificationType(NotificationType.TEAM);
        notificationRepository.save(modelMapper.map(notificationEventDTO, Notification.class));
    }

    @Override
    public void updateTeamNotification(TeamUpdateEventDTO teamUpdateEventDTO, String subject, String type) {
        System.out.println("#### -> Creating notification -> " + teamUpdateEventDTO);
        if(teamUpdateEventDTO.getCollaboratorAssignmentType().equals("New")) {
            subject = subject == null ? "team-created" : subject;
        } else if(teamUpdateEventDTO.getCollaboratorAssignmentType().equals("Removed")) {
            subject = subject == null ? "removed-from-team" : subject;
        } else if(teamUpdateEventDTO.getCollaboratorAssignmentType().equals("Existing")) {
            subject = subject == null ? "team-changed" : subject;
        }

        NotificationEventDTO notificationEventDTO = new NotificationEventDTO();
        notificationEventDTO.setNotificationName(teamUpdateEventDTO.getTeamName());
        notificationEventDTO.setAssignerId(teamUpdateEventDTO.getAssignerId());
        notificationEventDTO.setCollaboratorIds(teamUpdateEventDTO.getCollaboratorIds());
        notificationEventDTO.setSubject(subject);
        notificationEventDTO.setNotificationType(NotificationType.TEAM);
        notificationRepository.save(modelMapper.map(notificationEventDTO, Notification.class));
    }

    @Override
    public void deleteTeamNotification(TeamDeleteEventDTO teamDeleteEventDTO, String subject, String type) {
        System.out.println("#### -> Creating notification -> " + teamDeleteEventDTO);
        subject = subject == null ? "team-removed" : subject;
        NotificationEventDTO notificationEventDTO = new NotificationEventDTO();
        notificationEventDTO.setNotificationName(teamDeleteEventDTO.getTeamName());
        notificationEventDTO.setAssignerId(teamDeleteEventDTO.getAssignerId());
        notificationEventDTO.setCollaboratorIds(teamDeleteEventDTO.getCollaboratorIds());
        notificationEventDTO.setSubject(subject);
        notificationEventDTO.setNotificationType(NotificationType.TEAM);
        notificationRepository.save(modelMapper.map(notificationEventDTO, Notification.class));
    }


    //    Work Inbox Notification
    @Override
    public void createWorkNotification(WorkCreateEventDTO workCreateEventDTO, String subject, String type) {
        System.out.println("#### -> Creating notification to db -> " + workCreateEventDTO);
        subject = subject == null ? "work-created" : subject;
        NotificationEventDTO notificationEventDTO = new NotificationEventDTO();
        notificationEventDTO.setNotificationName(workCreateEventDTO.getWorkName());
        notificationEventDTO.setAssignerId(workCreateEventDTO.getAssignerId());
        notificationEventDTO.setCollaboratorIds(workCreateEventDTO.getCollaboratorIds());
        notificationEventDTO.setSubject(subject);
        notificationEventDTO.setNotificationType(NotificationType.WORK);
        notificationRepository.save(modelMapper.map(notificationEventDTO, Notification.class));
    }

    @Override
    public void updateWorkNotification(WorkUpdateEventDTO workUpdateEventDTO, String subject, String type) {
        System.out.println("#### -> update work notification -> " + workUpdateEventDTO);
        if(workUpdateEventDTO.getCollaboratorAssignmentType().equals("New")) {
            subject = subject == null ? "work-created" : subject;
        } else if(workUpdateEventDTO.getCollaboratorAssignmentType().equals("Removed")) {
            subject = subject == null ? "removed-from-work" : subject;
        } else if(workUpdateEventDTO.getCollaboratorAssignmentType().equals("Existing")) {
            subject = subject == null ? "work-changed" : subject;
        }

        NotificationEventDTO notificationEventDTO = new NotificationEventDTO();
        notificationEventDTO.setNotificationName(workUpdateEventDTO.getWorkName());
        notificationEventDTO.setAssignerId(workUpdateEventDTO.getAssignerId());
        notificationEventDTO.setCollaboratorIds(workUpdateEventDTO.getCollaboratorIds());
        notificationEventDTO.setSubject(subject);
        notificationEventDTO.setNotificationType(NotificationType.WORK);
        notificationRepository.save(modelMapper.map(notificationEventDTO, Notification.class));
    }

    @Override
    public void deleteWorkNotification(WorkDeleteEventDTO workDeleteEventDTO, String subject, String type) {
        System.out.println("#### -> Creating notification -> " + workDeleteEventDTO);
        subject = subject == null ? "team-removed" : subject;
        NotificationEventDTO notificationEventDTO = new NotificationEventDTO();
        notificationEventDTO.setNotificationName(workDeleteEventDTO.getWorkName());
        notificationEventDTO.setAssignerId(workDeleteEventDTO.getAssignerId());
        notificationEventDTO.setCollaboratorIds(workDeleteEventDTO.getCollaboratorIds());
        notificationEventDTO.setSubject(subject);
        notificationEventDTO.setNotificationType(NotificationType.WORK);
        notificationRepository.save(modelMapper.map(notificationEventDTO, Notification.class));
    }

}
