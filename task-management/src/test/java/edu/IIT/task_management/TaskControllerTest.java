package edu.IIT.task_management;

import edu.IIT.task_management.controller.TaskController;
import edu.IIT.task_management.dto.*;
import edu.IIT.task_management.service.TaskService;
import edu.IIT.task_management.producer.TaskProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class TaskControllerTest {

    @InjectMocks
    private TaskController taskController;

    @Mock
    private TaskService taskService;

    @Mock
    private TaskProducer taskProducer;

    @BeforeEach
    void setUp() {
        // Initialize the mocks
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddTask() {
        // Arrange
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTaskName("Test Task");
        taskDTO.setAssignerId(1);
        taskDTO.setDueDate(LocalDate.now());

        // Mock service behavior
        when(taskService.createTask(any(TaskDTO.class))).thenReturn("Task created successfully");

        // Act
        ResponseEntity<String> response = taskController.addTask(taskDTO);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Task created successfully", response.getBody());

        // Verify interactions
        verify(taskProducer).sendMessage(taskDTO);
        verify(taskService).createTask(taskDTO);
    }

    @Test
    void testAddTaskWhenServiceFails() {
        // Arrange
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTaskName("Test Task");
        taskDTO.setAssignerId(1);
        taskDTO.setDueDate(LocalDate.now());

        // Mock service to return a failure response
        when(taskService.createTask(any(TaskDTO.class))).thenReturn("Error creating task");

        // Act
        ResponseEntity<String> response = taskController.addTask(taskDTO);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Error creating task", response.getBody());

        // Verify interactions
        verify(taskProducer).sendMessage(taskDTO);
        verify(taskService).createTask(taskDTO);
    }

    @Test
    void testUpdateTask() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTaskName("Updated Task");

        // Mock the service method
        when(taskService.updateTask(taskDTO)).thenReturn("Task updated successfully");

        // Call the controller method and wrap the response in a ResponseEntity
        taskController.updateTask(taskDTO);

        // Verify that the service method was called with the expected parameter
        verify(taskService).updateTask(taskDTO);
    }


    @Test
    void testDeleteTask() {
        int taskId = 1;
        when(taskService.deleteTask(taskId)).thenReturn("Task deleted successfully");

        taskController.deleteTask(taskId);

        verify(taskService).deleteTask(taskId);
    }

    @Test
    void testGetTask() {
        int taskId = 1;
        TaskDTO taskDTO = new TaskDTO();
        when(taskService.getTaskById(taskId)).thenReturn(taskDTO);

        taskController.getTask(taskId);
        verify(taskService).getTaskById(taskId);
    }


    @Test
    void testGetTasksByProjectId() {
        int projectId = 1;
        List<TaskDTO> taskList = List.of(new TaskDTO());
        when(taskService.getTasksByProjectId(projectId)).thenReturn(taskList);

        taskController.getTasksByProjectId(projectId);

        verify(taskService).getTasksByProjectId(projectId);
    }

    @Test
    void testGetTasksByWorkId() {
        int workId = 1;
        List<TaskDTO> taskList = List.of(new TaskDTO());
        when(taskService.getTasksByWorkId(workId)).thenReturn(taskList);

        taskController.getTasksByWorkId(workId);

        verify(taskService).getTasksByWorkId(workId);
    }

    @Test
    void testGetAllTasks() {
        List<TaskDTO> taskList = List.of(new TaskDTO());
        when(taskService.getAllTasks()).thenReturn(taskList);

        taskController.getAllTasks();

        verify(taskService).getAllTasks();
    }

    @Test
    void testChangeTaskStatus() {
        int taskId = 1;
        doNothing().when(taskService).changeTaskStatus(taskId);

        taskController.changeTaskStatus(taskId);

        verify(taskService).changeTaskStatus(taskId);
    }

    @Test
    void testCreateTaskTemplate() {
        TemplateDTO taskTemplateDTO = new TemplateDTO();
        when(taskService.createTaskTemplate(taskTemplateDTO)).thenReturn("Task template created successfully");

        taskController.addTaskTemplate(taskTemplateDTO);

        verify(taskService).createTaskTemplate(taskTemplateDTO);
    }

    @Test
    void testUpdateTaskTemplate() {
        TemplateDTO taskTemplateDTO = new TemplateDTO();
        when(taskService.updateTaskTemplate(taskTemplateDTO)).thenReturn("Task template updated successfully");

        taskController.updateTaskTemplate(taskTemplateDTO);

        verify(taskService).updateTaskTemplate(taskTemplateDTO);
    }

    @Test
    void testDeleteTaskTemplate() {
        int taskTemplateId = 1;
        when(taskService.deleteTaskTemplate(taskTemplateId)).thenReturn("Task template deleted successfully");

        taskController.deleteTaskTemplate(taskTemplateId);

        verify(taskService).deleteTaskTemplate(taskTemplateId);
    }


    @Test
    void testGetTaskTemplate() {
        int taskTemplateId = 1;
        TemplateDTO taskTemplateDTO = new TemplateDTO();
        when(taskService.getTaskTemplateById(taskTemplateId)).thenReturn(taskTemplateDTO);

        TemplateDTO response = taskController.getTaskTemplate(taskTemplateId);

        verify(taskService).getTaskTemplateById(taskTemplateId);
    }


    @Test
    void testCreateCollaboratorsBlock() {
        CollaboratorsBlockDTO collaboratorsBlockDTO = new CollaboratorsBlockDTO();
        doNothing().when(taskService).createCollaboratorsBlock(collaboratorsBlockDTO);

        taskController.createCollaboratorsBlock(collaboratorsBlockDTO);

        verify(taskService).createCollaboratorsBlock(collaboratorsBlockDTO);
    }

    @Test
    void testUpdateCollaboratorsBlock() {
        CollaboratorsBlockDTO collaboratorsBlockDTO = new CollaboratorsBlockDTO();
        when(taskService.updateCollaboratorsBlock(collaboratorsBlockDTO)).thenReturn("Collaborators block updated successfully");

        taskController.updateCollaboratorsBlock(collaboratorsBlockDTO);

        verify(taskService).updateCollaboratorsBlock(collaboratorsBlockDTO);
    }

    @Test
    void testGetCollaboratorsBlock() {
        int workId = 1;
        CollaboratorsBlockDTO collaboratorsBlockDTO = new CollaboratorsBlockDTO();
        when(taskService.getCollaboratorsBlockByWorkId(workId)).thenReturn(collaboratorsBlockDTO);

        taskController.getCollaboratorsBlock(workId);

        verify(taskService).getCollaboratorsBlockByWorkId(workId);
    }


    @Test
    void testCreateRule() {
        RuleDTO ruleDTO = new RuleDTO();
        when(taskService.createRule(ruleDTO)).thenReturn("Rule created successfully");

        taskController.createRule(ruleDTO);

        verify(taskService).createRule(ruleDTO);
    }


    @Test
    void testUpdateRule() {
        RuleDTO ruleDTO = new RuleDTO();
        when(taskService.updateRule(ruleDTO)).thenReturn("Rule updated successfully");

        taskController.updateRule(ruleDTO);

        verify(taskService).updateRule(ruleDTO);
    }

    @Test
    void testDeleteRule() {
        int ruleId = 1;
        when(taskService.deleteRule(ruleId)).thenReturn("Rule deleted successfully");

        taskController.deleteRule(ruleId);

        verify(taskService).deleteRule(ruleId);
    }

    @Test
    void testGetRule() {
        int ruleId = 1;
        RuleDTO ruleDTO = new RuleDTO();
        when(taskService.getRuleById(ruleId)).thenReturn(ruleDTO);

        taskController.getRule(ruleId);

        verify(taskService).getRuleById(ruleId);
    }

    @Test
    void testGetAllRules() {
        List<RuleDTO> ruleList = List.of(new RuleDTO());
        when(taskService.getAllRules()).thenReturn(ruleList);

        taskController.getAllRules();

        verify(taskService).getAllRules();
    }

    @Test
    void testGetRulesByProjectId() {
        int projectId = 1;
        List<RuleDTO> ruleList = List.of(new RuleDTO());
        when(taskService.getRulesByProjectId(projectId)).thenReturn(ruleList);

        taskController.getRulesByProjectId(projectId);

        verify(taskService).getRulesByProjectId(projectId);
    }

    @Test
    void testCreatePublishFlow() {
        PublishFlowDTO publishFlowDTO = new PublishFlowDTO();
        when(taskService.createPublishFlow(publishFlowDTO)).thenReturn("Publish flow created successfully");

        taskController.createPublishFlow(publishFlowDTO);

        verify(taskService).createPublishFlow(publishFlowDTO);
    }

    @Test
    void testUpdatePublishFlow() {
        PublishFlowDTO publishFlowDTO = new PublishFlowDTO();
        when(taskService.updatePublishFlow(publishFlowDTO)).thenReturn("Publish flow updated successfully");

        taskController.updatePublishFlow(publishFlowDTO);

        verify(taskService).updatePublishFlow(publishFlowDTO);
    }

    @Test
    void testDeletePublishFlow() {
        int publishFlowId = 1;
        when(taskService.deletePublishFlow(publishFlowId)).thenReturn("Publish flow deleted successfully");

        taskController.deletePublishFlow(publishFlowId);

        verify(taskService).deletePublishFlow(publishFlowId);
    }

    @Test
    void testGetPublishFlow() {
        int publishFlowId = 1;
        PublishFlowDTO publishFlowDTO = new PublishFlowDTO();
        when(taskService.getPublishFlowById(publishFlowId)).thenReturn(publishFlowDTO);

        taskController.getPublishFlow(publishFlowId);

        verify(taskService).getPublishFlowById(publishFlowId);
    }

    @Test
    void testGetPublishFlowByProjectId() {
        int projectId = 1;
        PublishFlowDTO publishFlowDTO = new PublishFlowDTO();
        when(taskService.findPublishFlowByProjectId(projectId)).thenReturn(publishFlowDTO);

        taskController.getPublishFlowByProjectId(projectId);

        verify(taskService).findPublishFlowByProjectId(projectId);
    }

    @Test
    void testMovedAndUpdateTask() {
        TaskDTO taskDTO = new TaskDTO();
        when(taskService.movedAndUpdateTask(taskDTO)).thenReturn("Task moved and updated successfully");

        taskController.movedAndUpdateTask(taskDTO);

        verify(taskService).movedAndUpdateTask(taskDTO);
    }

    @Test
    void testGetPublishFlowByRuleId() {
        int ruleId = 1;
        PublishFlowDTO publishFlowDTO = new PublishFlowDTO();
        when(taskService.findPublishFlowByRuleId(ruleId)).thenReturn(publishFlowDTO);

        taskController.getPublishFlowByRuleId(ruleId);

        verify(taskService).findPublishFlowByRuleId(ruleId);
    }

    @Test
    void testDeletePublishFlowByRuleId() {
        int ruleId = 1;
        doNothing().when(taskService).deletePublishFlowByRuleId(ruleId);

        taskController.deletePublishFlowByRuleId(ruleId);

        verify(taskService).deletePublishFlowByRuleId(ruleId);
    }
}
