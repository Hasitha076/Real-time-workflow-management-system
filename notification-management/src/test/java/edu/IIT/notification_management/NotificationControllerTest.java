package edu.IIT.notification_management;

import edu.IIT.notification_management.controller.NotificationController;
import edu.IIT.notification_management.dto.NotificationEventDTO;
import edu.IIT.notification_management.service.NotificationService;
import edu.IIT.notification_management.service.NotificationServiceQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationServiceQuery notificationServiceQuery;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllNotifications() {
        // Arrange
        NotificationEventDTO notification1 = new NotificationEventDTO();
        notification1.setNotificationId(1);
        notification1.setNotificationName("Notification 1");

        NotificationEventDTO notification2 = new NotificationEventDTO();
        notification2.setNotificationId(2);
        notification2.setNotificationName("Notification 2");

        List<NotificationEventDTO> mockNotifications = Arrays.asList(notification1, notification2);

        when(notificationServiceQuery.getAllNotifications()).thenReturn(mockNotifications);

        // Act
        List<NotificationEventDTO> result = notificationController.getAllNotifications();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Notification 1", result.get(0).getNotificationName());
        verify(notificationServiceQuery, times(1)).getAllNotifications();
    }

    @Test
    void testGetNotificationById() {
        // Arrange
        NotificationEventDTO notification = new NotificationEventDTO();
        notification.setNotificationId(1);
        notification.setNotificationName("Test Notification");

        when(notificationServiceQuery.getNotificationById(1)).thenReturn(notification);

        // Act
        NotificationEventDTO result = notificationController.getNotificationById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getNotificationId());
        assertEquals("Test Notification", result.getNotificationName());
        verify(notificationServiceQuery, times(1)).getNotificationById(1);
    }
}
