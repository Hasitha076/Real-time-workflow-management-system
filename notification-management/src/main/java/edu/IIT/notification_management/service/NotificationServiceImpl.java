package edu.IIT.notification_management.service;

import edu.IIT.project_management.dto.ProjectCreateEventDTO;
import edu.IIT.project_management.dto.ProjectDeleteEventDTO;
import edu.IIT.project_management.dto.ProjectUpdateEventDTO;
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
    private final WebClient webClientService;

    @Autowired
    private JavaMailSender javaMailSender;

    NotificationServiceImpl(WebClient.Builder webClientBuilder, ModelMapper modelMapper) {
        this.webClientService = webClientBuilder.baseUrl("http://localhost:8081/api/v1").build();
        this.modelMapper = modelMapper;
    }

    @Override
    public void sendEmails(ProjectCreateEventDTO projectCreateEventDTO) {
        List<Integer> ids = projectCreateEventDTO.getCollaboratorIds();

        List<String> recipients = webClientService.get()
                .uri(uriBuilder -> uriBuilder.path("/user/filterUsers") // Ensure path matches the controller
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

        List<String> recipients = webClientService.get()
                .uri(uriBuilder -> uriBuilder.path("/user/filterUsers") // Ensure path matches the controller
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

        List<String> recipients = webClientService.get()
                .uri(uriBuilder -> uriBuilder.path("/user/filterUsers") // Ensure path matches the controller
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

}
