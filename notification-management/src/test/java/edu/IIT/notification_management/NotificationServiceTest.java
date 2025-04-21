package edu.IIT.notification_management;

import edu.IIT.notification_management.dto.*;
import edu.IIT.notification_management.model.Notification;
import edu.IIT.notification_management.repository.NotificationRepository;
import edu.IIT.notification_management.service.NotificationService;
import edu.IIT.notification_management.service.NotificationServiceImpl;
import edu.IIT.user_management.dto.OTPRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Arrays;

import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private WebClient userWebClient;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private NotificationServiceImpl notificationService;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private NotificationService notificationServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notificationService = new NotificationServiceImpl(userWebClient, modelMapper);

        // inject the remaining dependencies manually
        notificationService.javaMailSender = mailSender;
        notificationService.notificationRepository = notificationRepository;
    }

    @Test
    void testSendOTP() {
        // Given
        OTPRequest otpRequest = new OTPRequest();
        otpRequest.setEmail("test@example.com");
        otpRequest.setOTP(123456);

        // When
        notificationService.sendOTP(otpRequest);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testCreateTaskNotification() {
        // Arrange
        TaskCreateEventDTO taskCreateEventDTO = new TaskCreateEventDTO();
        taskCreateEventDTO.setTaskName("Test Task");
        taskCreateEventDTO.setAssignerId(1);
        taskCreateEventDTO.setCollaboratorIds(Arrays.asList(101, 102));

        Notification dummyNotification = new Notification();

        // Mock modelMapper behavior
        when(modelMapper.map(any(NotificationEventDTO.class), eq(Notification.class)))
                .thenReturn(dummyNotification);

        // Act
        notificationService.createTaskNotification(taskCreateEventDTO, null, "task-created");

        // Assert
        verify(modelMapper, times(1)).map(any(NotificationEventDTO.class), eq(Notification.class));
        verify(notificationRepository, times(1)).save(dummyNotification);
    }

    @Test
    void testUpdateTaskNotification_NewCollaborator() {
        TaskUpdateEventDTO dto = new TaskUpdateEventDTO();
        dto.setTaskName("Task A");
        dto.setAssignerId(1);
        dto.setCollaboratorIds(Arrays.asList(101, 102));
        dto.setCollaboratorAssignmentType("New");

        Notification dummyNotification = new Notification();
        when(modelMapper.map(any(NotificationEventDTO.class), eq(Notification.class)))
                .thenReturn(dummyNotification);

        notificationService.updateTaskNotification(dto, null, "task-created");

        verify(modelMapper, times(1)).map(any(NotificationEventDTO.class), eq(Notification.class));
        verify(notificationRepository, times(1)).save(dummyNotification);
    }

    @Test
    void testUpdateTaskNotification_RemovedCollaborator() {
        TaskUpdateEventDTO dto = new TaskUpdateEventDTO();
        dto.setTaskName("Task B");
        dto.setAssignerId(2);
        dto.setCollaboratorIds(Arrays.asList(201));
        dto.setCollaboratorAssignmentType("Removed");

        Notification dummyNotification = new Notification();
        when(modelMapper.map(any(NotificationEventDTO.class), eq(Notification.class)))
                .thenReturn(dummyNotification);

        notificationService.updateTaskNotification(dto, null, "removed-from-task");

        verify(modelMapper, times(1)).map(any(NotificationEventDTO.class), eq(Notification.class));
        verify(notificationRepository, times(1)).save(dummyNotification);
    }

    @Test
    void testUpdateTaskNotification_ExistingCollaborator() {
        TaskUpdateEventDTO dto = new TaskUpdateEventDTO();
        dto.setTaskName("Task C");
        dto.setAssignerId(3);
        dto.setCollaboratorIds(Arrays.asList(301));
        dto.setCollaboratorAssignmentType("Existing");

        Notification dummyNotification = new Notification();
        when(modelMapper.map(any(NotificationEventDTO.class), eq(Notification.class)))
                .thenReturn(dummyNotification);

        notificationService.updateTaskNotification(dto, null, "task-changed");

        verify(modelMapper, times(1)).map(any(NotificationEventDTO.class), eq(Notification.class));
        verify(notificationRepository, times(1)).save(dummyNotification);
    }

    @Test
    void testDeleteTaskNotification() {
        // Given
        TaskDeleteEventDTO dto = new TaskDeleteEventDTO();
        dto.setTaskName("Obsolete Task");
        dto.setAssignerId(10);
        dto.setCollaboratorIds(Arrays.asList(111, 112));

        Notification dummyNotification = new Notification();

        // When
        when(modelMapper.map(any(NotificationEventDTO.class), eq(Notification.class)))
                .thenReturn(dummyNotification);

        // Act
        notificationService.deleteTaskNotification(dto, null, "task-removed");

        // Then
        verify(modelMapper, times(1)).map(any(NotificationEventDTO.class), eq(Notification.class));
        verify(notificationRepository, times(1)).save(dummyNotification);
    }

    @Test
    void testCreateProjectNotification() {
        // Given
        ProjectCreateEventDTO projectDTO = new ProjectCreateEventDTO();
        projectDTO.setProjectName("Apollo");
        projectDTO.setAssignerId(20);
        projectDTO.setCollaboratorIds(Arrays.asList(301, 302));

        Notification dummyNotification = new Notification();

        // When
        when(modelMapper.map(any(NotificationEventDTO.class), eq(Notification.class)))
                .thenReturn(dummyNotification);

        // Act
        notificationService.createProjectNotification(projectDTO, null, "project-created");

        // Then
        verify(modelMapper, times(1)).map(any(NotificationEventDTO.class), eq(Notification.class));
        verify(notificationRepository, times(1)).save(dummyNotification);
    }

    @Test
    void testUpdateProjectNotification() {
        // Given
        ProjectUpdateEventDTO projectUpdateEventDTO = new ProjectUpdateEventDTO();
        projectUpdateEventDTO.setProjectName("Apollo");
        projectUpdateEventDTO.setAssignerId(20);
        projectUpdateEventDTO.setCollaboratorIds(Arrays.asList(301, 302));
        projectUpdateEventDTO.setCollaboratorAssignmentType("New");

        Notification dummyNotification = new Notification();

        // When
        when(modelMapper.map(any(NotificationEventDTO.class), eq(Notification.class)))
                .thenReturn(dummyNotification);

        // Act
        notificationService.updateProjectNotification(projectUpdateEventDTO, null, "project-created");

        // Then
        verify(modelMapper, times(1)).map(any(NotificationEventDTO.class), eq(Notification.class));
        verify(notificationRepository, times(1)).save(dummyNotification);
    }

    @Test
    void testUpdateProjectNotificationWithRemovedCollaborator() {
        // Given
        ProjectUpdateEventDTO projectUpdateEventDTO = new ProjectUpdateEventDTO();
        projectUpdateEventDTO.setProjectName("Apollo");
        projectUpdateEventDTO.setAssignerId(20);
        projectUpdateEventDTO.setCollaboratorIds(Arrays.asList(301, 302));
        projectUpdateEventDTO.setCollaboratorAssignmentType("Removed");

        Notification dummyNotification = new Notification();

        // When
        when(modelMapper.map(any(NotificationEventDTO.class), eq(Notification.class)))
                .thenReturn(dummyNotification);

        // Act
        notificationService.updateProjectNotification(projectUpdateEventDTO, null, "removed-from-project");

        // Then
        verify(modelMapper, times(1)).map(any(NotificationEventDTO.class), eq(Notification.class));
        verify(notificationRepository, times(1)).save(dummyNotification);
    }

    @Test
    void testUpdateProjectNotificationWithExistingCollaborator() {
        // Given
        ProjectUpdateEventDTO projectUpdateEventDTO = new ProjectUpdateEventDTO();
        projectUpdateEventDTO.setProjectName("Apollo");
        projectUpdateEventDTO.setAssignerId(20);
        projectUpdateEventDTO.setCollaboratorIds(Arrays.asList(301, 302));
        projectUpdateEventDTO.setCollaboratorAssignmentType("Existing");

        Notification dummyNotification = new Notification();

        // When
        when(modelMapper.map(any(NotificationEventDTO.class), eq(Notification.class)))
                .thenReturn(dummyNotification);

        // Act
        notificationService.updateProjectNotification(projectUpdateEventDTO, null, "project-changed");

        // Then
        verify(modelMapper, times(1)).map(any(NotificationEventDTO.class), eq(Notification.class));
        verify(notificationRepository, times(1)).save(dummyNotification);
    }

    @Test
    void testDeleteProjectNotification() {
        // Given
        ProjectDeleteEventDTO projectDeleteEventDTO = new ProjectDeleteEventDTO();
        projectDeleteEventDTO.setProjectName("Apollo");
        projectDeleteEventDTO.setAssignerId(20);
        projectDeleteEventDTO.setCollaboratorIds(Arrays.asList(301, 302));

        Notification dummyNotification = new Notification();

        // When
        when(modelMapper.map(any(NotificationEventDTO.class), eq(Notification.class)))
                .thenReturn(dummyNotification);

        // Act
        notificationService.deleteProjectNotification(projectDeleteEventDTO, null, "project-removed");

        // Then
        verify(modelMapper, times(1)).map(any(NotificationEventDTO.class), eq(Notification.class));
        verify(notificationRepository, times(1)).save(dummyNotification);
    }

    @Test
    void testCreateTeamNotification() {
        // Given
        TeamCreateEventDTO teamCreateEventDTO = new TeamCreateEventDTO();
        teamCreateEventDTO.setTeamName("Development Team");
        teamCreateEventDTO.setAssignerId(15);
        teamCreateEventDTO.setCollaboratorIds(Arrays.asList(301, 302, 303));

        Notification dummyNotification = new Notification();

        // When
        when(modelMapper.map(any(NotificationEventDTO.class), eq(Notification.class)))
                .thenReturn(dummyNotification);

        // Act
        notificationService.createTeamNotification(teamCreateEventDTO, null, "team-created");

        // Then
        verify(modelMapper, times(1)).map(any(NotificationEventDTO.class), eq(Notification.class));
        verify(notificationRepository, times(1)).save(dummyNotification);
    }

    @Test
    void testUpdateTeamNotification() {
        // Given
        TeamUpdateEventDTO teamUpdateEventDTO = new TeamUpdateEventDTO();
        teamUpdateEventDTO.setTeamName("Development Team");
        teamUpdateEventDTO.setAssignerId(15);
        teamUpdateEventDTO.setCollaboratorIds(Arrays.asList(301, 302, 303));
        teamUpdateEventDTO.setCollaboratorAssignmentType("New");

        Notification dummyNotification = new Notification();

        // When
        when(modelMapper.map(any(NotificationEventDTO.class), eq(Notification.class)))
                .thenReturn(dummyNotification);

        // Act
        notificationService.updateTeamNotification(teamUpdateEventDTO, null, "team-created");

        // Then
        verify(modelMapper, times(1)).map(any(NotificationEventDTO.class), eq(Notification.class));
        verify(notificationRepository, times(1)).save(dummyNotification);
    }

    @Test
    void testDeleteTeamNotification() {
        // Given
        TeamDeleteEventDTO teamDeleteEventDTO = new TeamDeleteEventDTO();
        teamDeleteEventDTO.setTeamName("Development Team");
        teamDeleteEventDTO.setAssignerId(15);
        teamDeleteEventDTO.setCollaboratorIds(Arrays.asList(301, 302));

        Notification dummyNotification = new Notification();

        // When
        when(modelMapper.map(any(NotificationEventDTO.class), eq(Notification.class)))
                .thenReturn(dummyNotification);

        // Act
        notificationService.deleteTeamNotification(teamDeleteEventDTO, null, "team-removed");

        // Then
        verify(modelMapper, times(1)).map(any(NotificationEventDTO.class), eq(Notification.class));
        verify(notificationRepository, times(1)).save(dummyNotification);
    }

    @Test
    void testCreateWorkNotification() {
        // Given
        WorkCreateEventDTO workCreateEventDTO = new WorkCreateEventDTO();
        workCreateEventDTO.setWorkName("Development Work");
        workCreateEventDTO.setAssignerId(15);
        workCreateEventDTO.setCollaboratorIds(Arrays.asList(301, 302, 303));

        Notification dummyNotification = new Notification();

        // When
        when(modelMapper.map(any(NotificationEventDTO.class), eq(Notification.class)))
                .thenReturn(dummyNotification);

        // Act
        notificationService.createWorkNotification(workCreateEventDTO, null, "work-created");

        // Then
        verify(modelMapper, times(1)).map(any(NotificationEventDTO.class), eq(Notification.class));
        verify(notificationRepository, times(1)).save(dummyNotification);
    }

    @Test
    void testUpdateWorkNotification() {
        // Given
        WorkUpdateEventDTO workUpdateEventDTO = new WorkUpdateEventDTO();
        workUpdateEventDTO.setWorkName("Development Work");
        workUpdateEventDTO.setAssignerId(15);
        workUpdateEventDTO.setCollaboratorIds(Arrays.asList(301, 302, 303));
        workUpdateEventDTO.setCollaboratorAssignmentType("New");

        Notification dummyNotification = new Notification();

        // When
        when(modelMapper.map(any(NotificationEventDTO.class), eq(Notification.class)))
                .thenReturn(dummyNotification);

        // Act
        notificationService.updateWorkNotification(workUpdateEventDTO, null, "work-created");

        // Then
        verify(modelMapper, times(1)).map(any(NotificationEventDTO.class), eq(Notification.class));
        verify(notificationRepository, times(1)).save(dummyNotification);
    }

    @Test
    void testDeleteWorkNotification() {
        // Given
        WorkDeleteEventDTO workDeleteEventDTO = new WorkDeleteEventDTO();
        workDeleteEventDTO.setWorkName("Development Work");
        workDeleteEventDTO.setAssignerId(15);
        workDeleteEventDTO.setCollaboratorIds(Arrays.asList(301, 302));

        Notification dummyNotification = new Notification();

        // When
        when(modelMapper.map(any(NotificationEventDTO.class), eq(Notification.class)))
                .thenReturn(dummyNotification);

        // Act
        notificationService.deleteWorkNotification(workDeleteEventDTO, null, "work-removed");

        // Then
        verify(modelMapper, times(1)).map(any(NotificationEventDTO.class), eq(Notification.class));
        verify(notificationRepository, times(1)).save(dummyNotification);
    }

}
