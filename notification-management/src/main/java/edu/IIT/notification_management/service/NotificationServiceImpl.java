package edu.IIT.notification_management.service;

import edu.IIT.project_management.dto.ProjectEventDTO;
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
    public void sendEmails(ProjectEventDTO projectEventDTO) {
        List<Integer> ids = projectEventDTO.getCollaboratorIds();

        List<String> recipients = webClientService.get()
                .uri(uriBuilder -> uriBuilder.path("/user/filterUsers") // Ensure path matches the controller
                        .queryParam("ids", ids) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        log.info("#### -> Sending email -> {}", recipients);

        String subject = "New Project Created: " + projectEventDTO.getProjectName();
        String body = "You have been added as a collaborator to the project: " + projectEventDTO.getProjectName();

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
