package edu.IIT.notification_management.service;

import edu.IIT.notification_management.dto.NotificationEventDTO;
import edu.IIT.notification_management.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceQuery {

    private final NotificationRepository notificationRepository;
    private final ModelMapper modelMapper;

    public List<NotificationEventDTO> getAllNotifications() {
        return modelMapper.map(notificationRepository.findAll(), new TypeToken<List<NotificationEventDTO>>() {
        }.getType());
    }

    public NotificationEventDTO getNotificationById(int id) {
        return modelMapper.map(notificationRepository.findById(id), NotificationEventDTO.class);
    }
}
