package edu.IIT.task_management;

import edu.IIT.task_management.dto.PublishFlowDTO;
import edu.IIT.task_management.dto.TaskDTO;
import edu.IIT.task_management.dto.TaskPriorityLevel;
import edu.IIT.task_management.model.PublishFlow;
import edu.IIT.task_management.model.Task;
import edu.IIT.task_management.producer.TaskProducer;
import edu.IIT.task_management.repository.PublishFlowRepository;
import edu.IIT.task_management.repository.TaskRepository;
import edu.IIT.task_management.service.TaskServiceImpl;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Mock
    private PublishFlowRepository publishFlowRepository;

    @Mock
    private TaskProducer taskProducer;


    @Test
    public void testGetAllTasks() {
        // When: Calling getAllTasks
        taskService.getAllTasks();

        // Then: Verify that findAll was called once
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    public void testCreateTask() {
        // Given: A new taskDTO
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTaskId(1);
        taskDTO.setTaskName("Task 1");
        taskDTO.setDescription("Description 1");
        taskDTO.setStatus(false);
        taskDTO.setAssignerId(1);
        taskDTO.setDueDate(LocalDate.of(2024, 4, 28));
        taskDTO.setPriority(TaskPriorityLevel.HIGH);
        taskDTO.setProjectId(1);
        taskDTO.setWorkId(1);
        taskDTO.setCollaboratorIds(List.of(1, 2, 3));
        taskDTO.setTeamIds(List.of(1, 4, 5));
        taskDTO.setMemberIcons(List.of("icon1.png", "icon2.png"));
        taskDTO.setTags(List.of("urgent", "feature"));
        taskDTO.setComments(List.of("Initial comment"));
        taskDTO.setCreatedAt(LocalDateTime.of(2021, 9, 1, 0, 0));
        taskDTO.setUpdatedAt(LocalDateTime.of(2021, 9, 10, 0, 0));

        Task task = new Task(); // Create the corresponding task entity object
        task.setTaskId(taskDTO.getTaskId());
        task.setTaskName(taskDTO.getTaskName());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.isStatus());
        task.setAssignerId(taskDTO.getAssignerId());
        task.setDueDate(taskDTO.getDueDate());
        task.setPriority(taskDTO.getPriority());
        task.setProjectId(taskDTO.getProjectId());
        task.setWorkId(taskDTO.getWorkId());
        task.setCollaboratorIds(taskDTO.getCollaboratorIds());
        task.setTeamIds(taskDTO.getTeamIds());
        task.setMemberIcons(taskDTO.getMemberIcons());
        task.setTags(taskDTO.getTags());
        task.setComments(taskDTO.getComments());
        task.setCreatedAt(taskDTO.getCreatedAt());
        task.setUpdatedAt(taskDTO.getUpdatedAt());

        // Stub modelMapper.map() to return the correct task object
        when(modelMapper.map(taskDTO, Task.class)).thenReturn(task);

        // Stub repository save method
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // When: Calling createTask with TaskDTO
        taskService.createTask(taskDTO);

        // Then: Verify that save was called once
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    public void testFindPublishFlowByProjectId() {
        // Given
        int projectId = 101;

        PublishFlow mockFlow = new PublishFlow();
        mockFlow.setPublishFlowId(1);
        mockFlow.setProjectId(projectId);
        mockFlow.setPublishFlowName("Test Flow");

        PublishFlowDTO mockFlowDTO = new PublishFlowDTO();
        mockFlowDTO.setPublishFlowId(1);
        mockFlowDTO.setProjectId(projectId);
        mockFlowDTO.setPublishFlowName("Test Flow");

        // Stub repository and mapper
        when(publishFlowRepository.findPublishFlowsByProjectId(projectId)).thenReturn(List.of(mockFlow));
        when(modelMapper.map(any(PublishFlow.class), eq(PublishFlowDTO.class))).thenReturn(mockFlowDTO);

        // When
        PublishFlowDTO result = taskService.findPublishFlowByProjectId(projectId);

        // Then
        assertNotNull(result);
        assertEquals(mockFlowDTO.getPublishFlowId(), result.getPublishFlowId());
        assertEquals(mockFlowDTO.getPublishFlowName(), result.getPublishFlowName());

        verify(publishFlowRepository, times(1)).findPublishFlowsByProjectId(projectId);
        verify(modelMapper, times(1)).map(any(PublishFlow.class), eq(PublishFlowDTO.class));
    }

    @Test
    public void testGetTaskById() {
        // Given
        int taskId = 10;
        Task task = new Task();
        task.setTaskId(taskId);
        task.setTaskName("Sample Task");

        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTaskId(taskId);
        taskDTO.setTaskName("Sample Task");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(modelMapper.map(task, TaskDTO.class)).thenReturn(taskDTO); // <--- Correct match

        // When
        TaskDTO result = taskService.getTaskById(taskId);

        // Then
        assertNotNull(result);
        assertEquals(taskId, result.getTaskId());
        assertEquals("Sample Task", result.getTaskName());

        verify(taskRepository, times(1)).findById(taskId);
        verify(modelMapper, times(1)).map(task, TaskDTO.class);
    }

    @Test
    public void testDeleteTask() {

        int taskId = 1;
        Task task = new Task();
        task.setTaskId(taskId);
        task.setTaskName("Sample Task");
        task.setAssignerId(1); // mock value
        task.setCollaboratorIds(List.of(2, 3)); // mock value

        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTaskId(taskId);
        taskDTO.setTaskName("Sample Task");
        taskDTO.setAssignerId(1); // same mock value
        taskDTO.setCollaboratorIds(List.of(2, 3)); // same mock value

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(modelMapper.map(task, TaskDTO.class)).thenReturn(taskDTO);
        doNothing().when(taskRepository).deleteById(taskId);
        doNothing().when(taskProducer).sendDeleteTaskMessage(
                eq(taskDTO.getTaskName()), eq(taskDTO.getAssignerId()), eq(taskDTO.getCollaboratorIds()));

        String result = taskService.deleteTask(taskId);

        assertEquals("Successfully deleted", result);
        verify(taskRepository, times(1)).deleteById(taskId);
        verify(taskProducer, times(1)).sendDeleteTaskMessage(
                eq(taskDTO.getTaskName()), eq(taskDTO.getAssignerId()), eq(taskDTO.getCollaboratorIds()));
    }

    @Test
    public void testDeleteByProjectId() {
        int projectId = 100;

        Task task1 = new Task();
        task1.setTaskId(1);
        task1.setTaskName("Task One");
        task1.setAssignerId(1); // mock value
        task1.setCollaboratorIds(List.of(2, 3)); // mock value

        Task task2 = new Task();
        task2.setTaskId(2);
        task2.setTaskName("Task Two");
        task2.setAssignerId(4); // mock value
        task2.setCollaboratorIds(List.of(5, 6)); // mock value

        List<Task> tasks = List.of(task1, task2);

        when(taskRepository.findByProjectId(projectId)).thenReturn(tasks);
        doNothing().when(taskRepository).deleteById(anyInt());

        doNothing().when(taskProducer).sendDeleteTaskMessage(
                eq(task1.getTaskName()), eq(task1.getAssignerId()), eq(task1.getCollaboratorIds()));
        doNothing().when(taskProducer).sendDeleteTaskMessage(
                eq(task2.getTaskName()), eq(task2.getAssignerId()), eq(task2.getCollaboratorIds()));

        taskService.deleteByProjectId(projectId);

        verify(taskRepository, times(1)).findByProjectId(projectId);
        verify(taskRepository, times(1)).deleteById(task1.getTaskId());
        verify(taskRepository, times(1)).deleteById(task2.getTaskId());

        verify(taskProducer, times(1)).sendDeleteTaskMessage(
                eq(task1.getTaskName()), eq(task1.getAssignerId()), eq(task1.getCollaboratorIds()));
        verify(taskProducer, times(1)).sendDeleteTaskMessage(
                eq(task2.getTaskName()), eq(task2.getAssignerId()), eq(task2.getCollaboratorIds()));
    }


}