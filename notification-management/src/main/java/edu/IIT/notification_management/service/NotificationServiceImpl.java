package edu.IIT.notification_management.service;

import edu.IIT.notification_management.dto.NotificationDTO;
import edu.IIT.project_management.dto.ProjectEventDTO;
import edu.IIT.user_management.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.util.List;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final ModelMapper modelMapper;
    private final WebClient webClientService;

    NotificationServiceImpl(WebClient.Builder webClientBuilder, ModelMapper modelMapper) {
        this.webClientService = webClientBuilder.baseUrl("http://localhost:8081/api/v1").build();
        this.modelMapper = modelMapper;
    }

    @Override
    public void sendEmails(ProjectEventDTO projectEventDTO) {
        List<Integer> ids = projectEventDTO.getCollaboratorIds();

        List userList = webClientService.get()
                .uri(uriBuilder -> uriBuilder.path("/user/filterUsers") // Ensure path matches the controller
                        .queryParam("ids", ids) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        log.info("#### -> Sending email -> {}", userList);
    }
}
