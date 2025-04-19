package edu.IIT.task_management;

import edu.IIT.task_management.dto.*;
import edu.IIT.task_management.model.*;
import edu.IIT.task_management.producer.TaskProducer;
import edu.IIT.task_management.repository.*;
import edu.IIT.task_management.service.TaskService;
import edu.IIT.task_management.service.TaskServiceImpl;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
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

    @Mock
    private WebClient workWebClient;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private CollaboratorsBlockRepository collaboratorsBlockRepository;

    @Mock
    private RuleRepository ruleRepository;


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

    @Test
    public void testDeleteByWorkId() {
        int workId = 200;

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

        when(taskRepository.findByWorkId(workId)).thenReturn(tasks);
        doNothing().when(taskRepository).deleteById(anyInt());

        doNothing().when(taskProducer).sendDeleteTaskMessage(
                eq(task1.getTaskName()), eq(task1.getAssignerId()), eq(task1.getCollaboratorIds()));
        doNothing().when(taskProducer).sendDeleteTaskMessage(
                eq(task2.getTaskName()), eq(task2.getAssignerId()), eq(task2.getCollaboratorIds()));

        taskService.deleteByWorkId(workId);

        verify(taskRepository, times(1)).findByWorkId(workId);
        verify(taskRepository, times(1)).deleteById(task1.getTaskId());
        verify(taskRepository, times(1)).deleteById(task2.getTaskId());

        verify(taskProducer, times(1)).sendDeleteTaskMessage(
                eq(task1.getTaskName()), eq(task1.getAssignerId()), eq(task1.getCollaboratorIds()));
        verify(taskProducer, times(1)).sendDeleteTaskMessage(
                eq(task2.getTaskName()), eq(task2.getAssignerId()), eq(task2.getCollaboratorIds()));
    }

    @Test
    public void testGetTasksByProjectId_WithTasks() {
        int projectId = 101;

        Task task1 = new Task();
        task1.setTaskId(1);
        task1.setTaskName("Task A");

        Task task2 = new Task();
        task2.setTaskId(2);
        task2.setTaskName("Task B");

        List<Task> taskList = List.of(task1, task2);

        TaskDTO taskDTO1 = new TaskDTO();
        taskDTO1.setTaskId(1);
        taskDTO1.setTaskName("Task A");

        TaskDTO taskDTO2 = new TaskDTO();
        taskDTO2.setTaskId(2);
        taskDTO2.setTaskName("Task B");

        List<TaskDTO> taskDTOList = List.of(taskDTO1, taskDTO2);

        when(taskRepository.findByProjectId(projectId)).thenReturn(taskList);
        when(modelMapper.map(eq(taskList), any(Type.class))).thenReturn(taskDTOList);

        List<TaskDTO> result = taskService.getTasksByProjectId(projectId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Task A", result.get(0).getTaskName());
        assertEquals("Task B", result.get(1).getTaskName());

        verify(taskRepository, times(1)).findByProjectId(projectId);
        verify(modelMapper, times(1)).map(eq(taskList), any(Type.class));
    }

    @Test
    public void testGetTasksByProjectId_NoTasks() {
        int projectId = 102;

        when(taskRepository.findByProjectId(projectId)).thenReturn(Collections.emptyList());

        List<TaskDTO> result = taskService.getTasksByProjectId(projectId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository, times(1)).findByProjectId(projectId);
        verify(modelMapper, times(0)).map(any(), any()); // Should not call map if list is empty
    }

    @Test
    public void testGetTasksByWorkId_WithTasks() {
        int workId = 201;

        Task task1 = new Task();
        task1.setTaskId(1);
        task1.setTaskName("Task Alpha");

        Task task2 = new Task();
        task2.setTaskId(2);
        task2.setTaskName("Task Beta");

        List<Task> taskList = List.of(task1, task2);

        TaskDTO taskDTO1 = new TaskDTO();
        taskDTO1.setTaskId(1);
        taskDTO1.setTaskName("Task Alpha");

        TaskDTO taskDTO2 = new TaskDTO();
        taskDTO2.setTaskId(2);
        taskDTO2.setTaskName("Task Beta");

        List<TaskDTO> taskDTOList = List.of(taskDTO1, taskDTO2);

        when(taskRepository.findByWorkId(workId)).thenReturn(taskList);
        when(modelMapper.map(eq(taskList), any(Type.class))).thenReturn(taskDTOList);

        List<TaskDTO> result = taskService.getTasksByWorkId(workId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Task Alpha", result.get(0).getTaskName());
        assertEquals("Task Beta", result.get(1).getTaskName());

        verify(taskRepository, times(1)).findByWorkId(workId);
        verify(modelMapper, times(1)).map(eq(taskList), any(Type.class));
    }

    @Test
    public void testGetTasksByWorkId_NoTasks() {
        int workId = 202;

        List<Task> emptyTaskList = new ArrayList<>();
        List<TaskDTO> emptyTaskDTOList = new ArrayList<>();

        when(taskRepository.findByWorkId(workId)).thenReturn(emptyTaskList);
        when(modelMapper.map(eq(emptyTaskList), any(Type.class))).thenReturn(emptyTaskDTOList);

        List<TaskDTO> result = taskService.getTasksByWorkId(workId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository, times(1)).findByWorkId(workId);
        verify(modelMapper, times(1)).map(eq(emptyTaskList), any(Type.class));
    }

//    @SuppressWarnings("unchecked")
//    @Test
//    public void testChangeTaskStatus() {
//        int taskId = 1;
//        int projectId = 100;
//        int workId = 200;
//
//        // Mock Task
//        Task mockTask = new Task();
//        mockTask.setTaskId(taskId);
//        mockTask.setProjectId(projectId);
//        mockTask.setWorkId(workId);
//        mockTask.setStatus(false);
//
//        when(taskRepository.findById(taskId)).thenReturn(Optional.of(mockTask));
//
//        // WebClient mocking
//        WebClient.RequestHeadersUriSpec<?> uriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
//        WebClient.RequestHeadersSpec<?> headersSpecMock = mock(WebClient.RequestHeadersSpec.class);
//        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);
//
//        // Proper return chaining
//        when(workWebClient.get()).thenReturn(uriSpecMock);
//        when(uriSpecMock.uri(eq("/getWorksByProjectId/{id}"), eq(projectId))).thenReturn(headersSpecMock);
//        when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
//
//        // Prepare mocked body
//        List<Map<String, Object>> mockWorkList = List.of(
//                Map.of("workId", 200),
//                Map.of("workId", 201),
//                Map.of("workId", 202)
//        );
//
//        // Handle bodyToMono generic issue safely
//        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class)))
//                .thenReturn(Mono.just(mockWorkList));
//
//        // Mock publish flow
//        PublishFlow mockPublishFlow = new PublishFlow();
//        mockPublishFlow.setStatus("active");
//        mockPublishFlow.setTriggersJson("""
//        [{"triggerDetails": {"triggerType": "Status is changed"}}]
//    """);
//        mockPublishFlow.setActionsJson("""
//        [{"actionDetails": {"actionType": "Move task to section", "ActionMovedSection": {"workId": 205}}}]
//    """);
//
//        when(publishFlowRepository.findPublishFlowsByProjectId(projectId)).thenReturn(List.of(mockPublishFlow));
//
//        // Act
//        taskService.changeTaskStatus(taskId);
//
//        // Assert
//        verify(taskRepository).save(any(Task.class));
//        assertTrue(mockTask.isStatus());
//        assertEquals(205, mockTask.getWorkId());
//    }

    @Test
    public void testCreateTaskTemplate() {
        // Arrange
        TemplateDTO templateDTO = new TemplateDTO();
        templateDTO.setTaskTemplateName("Daily Scrum");
        templateDTO.setTaskTemplateDescription("Template for daily scrum tasks");

        Template mappedTemplate = new Template();
        mappedTemplate.setTaskTemplateName("Daily Scrum");
        mappedTemplate.setTaskTemplateDescription("Template for daily scrum tasks");

        // Mock the mapping
        when(modelMapper.map(templateDTO, Template.class)).thenReturn(mappedTemplate);

        // Mock the repository save call
        when(templateRepository.save(mappedTemplate)).thenReturn(mappedTemplate);

        // Act
        String result = taskService.createTaskTemplate(templateDTO);

        // Assert
        assertEquals("Task template created successfully", result);
        verify(templateRepository, times(1)).save(mappedTemplate);
    }


    @Test
    public void testGetTaskTemplateById() {
        int templateId = 1;

        Template mockTemplate = new Template();
        mockTemplate.setTaskTemplateName("Daily Scrum");
        mockTemplate.setTaskTemplateDescription("Template for daily scrum tasks");

        TemplateDTO expectedDTO = new TemplateDTO();
        expectedDTO.setTaskTemplateName("Daily Scrum");
        expectedDTO.setTaskTemplateDescription("Template for daily scrum tasks");

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(mockTemplate));
        when(modelMapper.map(mockTemplate, TemplateDTO.class)).thenReturn(expectedDTO);

        TemplateDTO result = taskService.getTaskTemplateById(templateId);

        assertNotNull(result);
        assertEquals("Daily Scrum", result.getTaskTemplateName());
        assertEquals("Template for daily scrum tasks", result.getTaskTemplateDescription());

        verify(templateRepository).findById(templateId);
        verify(modelMapper).map(mockTemplate, TemplateDTO.class);
    }

    @Test
    public void testGetTaskTemplatesByProjectId() {
        int projectId = 100;

        Template template1 = new Template();
        template1.setTaskTemplateName("Daily Standup");
        template1.setProjectId(projectId);

        Template template2 = new Template();
        template2.setTaskTemplateName("Sprint Planning");
        template2.setProjectId(projectId);

        List<Template> mockTemplates = List.of(template1, template2);

        TemplateDTO dto1 = new TemplateDTO();
        dto1.setTaskTemplateName("Daily Standup");

        TemplateDTO dto2 = new TemplateDTO();
        dto2.setTaskTemplateName("Sprint Planning");

        List<TemplateDTO> expectedDTOs = List.of(dto1, dto2);

        when(templateRepository.findByProjectId(projectId)).thenReturn(mockTemplates);
        when(modelMapper.map(eq(mockTemplates), any(Type.class))).thenReturn(expectedDTOs);

        List<TemplateDTO> result = taskService.getTaskTemplatesByProjectId(projectId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Daily Standup", result.get(0).getTaskTemplateName());
        assertEquals("Sprint Planning", result.get(1).getTaskTemplateName());

        verify(templateRepository).findByProjectId(projectId);
        verify(modelMapper).map(eq(mockTemplates), any(Type.class));
    }

    @Test
    public void testUpdateTaskTemplate() {
        // Arrange
        TemplateDTO templateDTO = new TemplateDTO();
        templateDTO.setTaskTemplateId(1);
        templateDTO.setTaskTemplateName("Updated Template");
        templateDTO.setTaskTemplateDescription("Updated Description");

        Template mappedTemplate = new Template();
        mappedTemplate.setTaskTemplateId(1);
        mappedTemplate.setTaskTemplateName("Updated Template");
        mappedTemplate.setTaskTemplateDescription("Updated Description");

        // Mock the mapping
        when(modelMapper.map(eq(templateDTO), any(Type.class))).thenReturn(mappedTemplate);

        // Mock save (no need to mock return value as it's void)
        when(templateRepository.save(mappedTemplate)).thenReturn(mappedTemplate);

        // Act
        String result = taskService.updateTaskTemplate(templateDTO);

        // Assert
        assertEquals("Task template updated successfully", result);
        verify(modelMapper).map(eq(templateDTO), any(Type.class));
        verify(templateRepository).save(mappedTemplate);
    }

    @Test
    public void testDeleteTaskTemplate() {
        // Arrange
        int templateId = 1;

        // Act
        taskService.deleteTaskTemplate(templateId);

        // Assert
        verify(templateRepository, times(1)).deleteById(templateId);
    }

    @Test
    public void testGetAllTaskTemplates() {
        // Arrange
        Template template1 = new Template();
        template1.setTaskTemplateName("Template 1");
        template1.setTaskTemplateDescription("Description 1");

        Template template2 = new Template();
        template2.setTaskTemplateName("Template 2");
        template2.setTaskTemplateDescription("Description 2");

        List<Template> templateList = List.of(template1, template2);

        TemplateDTO dto1 = new TemplateDTO();
        dto1.setTaskTemplateName("Template 1");
        dto1.setTaskTemplateDescription("Description 1");

        TemplateDTO dto2 = new TemplateDTO();
        dto2.setTaskTemplateName("Template 2");
        dto2.setTaskTemplateDescription("Description 2");

        List<TemplateDTO> dtoList = List.of(dto1, dto2);

        Type targetType = new TypeToken<List<TemplateDTO>>() {}.getType();

        when(templateRepository.findAll()).thenReturn(templateList);
        when(modelMapper.map(templateList, targetType)).thenReturn(dtoList);

        // Act
        List<TemplateDTO> result = taskService.getAllTaskTemplates();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Template 1", result.get(0).getTaskTemplateName());
        assertEquals("Template 2", result.get(1).getTaskTemplateName());

        verify(templateRepository, times(1)).findAll();
        verify(modelMapper, times(1)).map(templateList, targetType);
    }

    @Test
    public void testCreateCollaboratorsBlock() {
        // Arrange
        CollaboratorsBlockDTO dto = new CollaboratorsBlockDTO();
        dto.setCollaboratorsBlockId(1);

        CollaboratorsBlock entity = new CollaboratorsBlock();
        entity.setCollaboratorsBlockId(1);

        when(modelMapper.map(dto, CollaboratorsBlock.class)).thenReturn(entity);

        // Act
        taskService.createCollaboratorsBlock(dto);

        // Assert
        verify(modelMapper, times(1)).map(dto, CollaboratorsBlock.class);
        verify(collaboratorsBlockRepository, times(1)).save(entity);
    }

    @Test
    public void testUpdateCollaboratorsBlock() {
        // Arrange
        CollaboratorsBlockDTO dto = new CollaboratorsBlockDTO();
        dto.setCollaboratorsBlockId(1);

        CollaboratorsBlock entity = new CollaboratorsBlock();
        entity.setCollaboratorsBlockId(1);

        // Mock ModelMapper conversion
        when(modelMapper.map(eq(dto), any(Type.class))).thenReturn(entity);

        // Act
        String result = taskService.updateCollaboratorsBlock(dto);

        // Assert
        assertEquals("Collaborators block updated successfully", result);
        verify(modelMapper, times(1)).map(eq(dto), any(Type.class));
        verify(collaboratorsBlockRepository, times(1)).save(entity);
    }

    @Test
    public void testGetCollaboratorsBlockByWorkId() {
        // Arrange
        int workId = 101;

        CollaboratorsBlock mockEntity = new CollaboratorsBlock();
        mockEntity.setWorkId(workId);
        mockEntity.setCollaboratorsBlockId(1);

        CollaboratorsBlockDTO expectedDTO = new CollaboratorsBlockDTO();
        expectedDTO.setWorkId(workId);
        expectedDTO.setCollaboratorsBlockId(1);

        when(collaboratorsBlockRepository.findByWorkId(workId)).thenReturn(mockEntity);
        when(modelMapper.map(mockEntity, CollaboratorsBlockDTO.class)).thenReturn(expectedDTO);

        // Act
        CollaboratorsBlockDTO result = taskService.getCollaboratorsBlockByWorkId(workId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDTO.getWorkId(), result.getWorkId());
        assertEquals(expectedDTO.getCollaboratorsBlockId(), result.getCollaboratorsBlockId());

        verify(collaboratorsBlockRepository, times(1)).findByWorkId(workId);
        verify(modelMapper, times(1)).map(mockEntity, CollaboratorsBlockDTO.class);
    }

    @Test
    public void testCreateRule() {
        // Arrange
        RuleDTO ruleDTO = new RuleDTO();
        ruleDTO.setRuleName("Sample Rule");
        ruleDTO.setStatus("active");

        Rule mappedRule = new Rule();
        mappedRule.setRuleName("Sample Rule");
        mappedRule.setStatus("active");

        // Mock the mapping from RuleDTO to Rule
        when(modelMapper.map(ruleDTO, Rule.class)).thenReturn(mappedRule);

        // Mock the save operation (no return needed since it's void)
        when(ruleRepository.save(mappedRule)).thenReturn(mappedRule);

        // Act
        String result = taskService.createRule(ruleDTO);

        // Assert
        assertEquals("Rule created successfully", result);
        verify(ruleRepository, times(1)).save(mappedRule);
    }

    @Test
    public void testGetRuleById() {
        // Arrange
        int ruleId = 1;
        Rule rule = new Rule();
        rule.setRuleName("Sample Rule");
        rule.setStatus("active");

        RuleDTO expectedRuleDTO = new RuleDTO();
        expectedRuleDTO.setRuleName("Sample Rule");
        expectedRuleDTO.setStatus("active");

        // Mock the ruleRepository to return the Rule object wrapped in Optional
        when(ruleRepository.findById(ruleId)).thenReturn(Optional.of(rule));

        // Mock the modelMapper to return the RuleDTO when mapping the Rule object
        // Notice: we're passing Optional<Rule> here as per the actual method call
        when(modelMapper.map(Optional.of(rule), RuleDTO.class)).thenReturn(expectedRuleDTO);

        // Act
        RuleDTO result = taskService.getRuleById(ruleId);

        // Assert
        assertNotNull(result);
        assertEquals("Sample Rule", result.getRuleName());
        assertEquals("active", result.getStatus());

        // Verify interactions with the mocks
        verify(ruleRepository, times(1)).findById(ruleId);
        verify(modelMapper, times(1)).map(Optional.of(rule), RuleDTO.class);
    }

    @Test
    public void testUpdateRule() {
        // Arrange
        RuleDTO ruleDTO = new RuleDTO();
        ruleDTO.setRuleName("Updated Rule");
        ruleDTO.setStatus("inactive");

        Rule rule = new Rule();
        rule.setRuleName("Updated Rule");
        rule.setStatus("inactive");

        // Mock the modelMapper to return the Rule object when mapping the RuleDTO object
        when(modelMapper.map(ruleDTO, new TypeToken<Rule>() {}.getType())).thenReturn(rule);

        // Mock the ruleRepository save method to return the saved rule
        when(ruleRepository.save(rule)).thenReturn(rule);

        // Act
        String result = taskService.updateRule(ruleDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Rule updated successfully", result);

        // Verify interactions with the mocks
        verify(modelMapper, times(1)).map(ruleDTO, new TypeToken<Rule>() {}.getType());
        verify(ruleRepository, times(1)).save(rule);
    }

    @Test
    public void testDeleteRule() {
        // Arrange
        int ruleId = 1;

        // Mock the ruleRepository deleteById method (no need to return anything as it's a void method)
        doNothing().when(ruleRepository).deleteById(ruleId);

        // Act
        taskService.deleteRule(ruleId);

        // Assert
        // Verify that deleteById was called once with the correct ID
        verify(ruleRepository, times(1)).deleteById(ruleId);
    }

    @Test
    public void testGetAllRules() {
        // Arrange
        List<Rule> ruleList = new ArrayList<>();
        Rule rule1 = new Rule();
        rule1.setRuleName("Rule 1");
        rule1.setStatus("active");

        Rule rule2 = new Rule();
        rule2.setRuleName("Rule 2");
        rule2.setStatus("inactive");

        ruleList.add(rule1);
        ruleList.add(rule2);

        RuleDTO ruleDTO1 = new RuleDTO();
        ruleDTO1.setRuleName("Rule 1");
        ruleDTO1.setStatus("active");

        RuleDTO ruleDTO2 = new RuleDTO();
        ruleDTO2.setRuleName("Rule 2");
        ruleDTO2.setStatus("inactive");

        List<RuleDTO> expectedRuleDTOList = new ArrayList<>();
        expectedRuleDTOList.add(ruleDTO1);
        expectedRuleDTOList.add(ruleDTO2);

        // Mock the ruleRepository to return the list of rules
        when(ruleRepository.findAll()).thenReturn(ruleList);

        // Mock the modelMapper to map the list of Rule objects to RuleDTO objects
        when(modelMapper.map(ruleList, new TypeToken<List<RuleDTO>>(){}.getType())).thenReturn(expectedRuleDTOList);

        // Act
        List<RuleDTO> result = taskService.getAllRules();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Rule 1", result.get(0).getRuleName());
        assertEquals("active", result.get(0).getStatus());
        assertEquals("Rule 2", result.get(1).getRuleName());
        assertEquals("inactive", result.get(1).getStatus());

        // Verify interactions with the mocks
        verify(ruleRepository, times(1)).findAll();
        verify(modelMapper, times(1)).map(ruleList, new TypeToken<List<RuleDTO>>(){}.getType());
    }

    @Test
    public void testGetRulesByProjectId() {
        // Arrange
        int projectId = 1;

        // Create a list of Rule objects
        List<Rule> ruleList = new ArrayList<>();

        Rule rule1 = new Rule();
        rule1.setRuleName("Project Rule 1");
        rule1.setStatus("active");

        Rule rule2 = new Rule();
        rule2.setRuleName("Project Rule 2");
        rule2.setStatus("inactive");

        ruleList.add(rule1);
        ruleList.add(rule2);

        // Create corresponding RuleDTO objects for verification
        RuleDTO ruleDTO1 = new RuleDTO();
        ruleDTO1.setRuleName("Project Rule 1");
        ruleDTO1.setStatus("active");

        RuleDTO ruleDTO2 = new RuleDTO();
        ruleDTO2.setRuleName("Project Rule 2");
        ruleDTO2.setStatus("inactive");

        List<RuleDTO> expectedRuleDTOList = new ArrayList<>();
        expectedRuleDTOList.add(ruleDTO1);
        expectedRuleDTOList.add(ruleDTO2);

        // Mock the ruleRepository to return the list of rules based on projectId
        when(ruleRepository.findRulesByProjectId(projectId)).thenReturn(ruleList);

        // Mock the modelMapper to map the list of Rule objects to RuleDTO objects
        when(modelMapper.map(ruleList, new TypeToken<List<RuleDTO>>(){}.getType())).thenReturn(expectedRuleDTOList);

        // Act
        List<RuleDTO> result = taskService.getRulesByProjectId(projectId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Project Rule 1", result.get(0).getRuleName());
        assertEquals("active", result.get(0).getStatus());
        assertEquals("Project Rule 2", result.get(1).getRuleName());
        assertEquals("inactive", result.get(1).getStatus());

        // Verify interactions with the mocks
        verify(ruleRepository, times(1)).findRulesByProjectId(projectId);
        verify(modelMapper, times(1)).map(ruleList, new TypeToken<List<RuleDTO>>(){}.getType());
    }

    @Test
    public void testCreatePublishFlow() {
        // Arrange
        PublishFlowDTO publishFlowDTO = new PublishFlowDTO();
        publishFlowDTO.setProjectId(1);
        publishFlowDTO.setPublishFlowName("Test Flow");

        PublishFlow publishFlow = new PublishFlow();
        publishFlow.setProjectId(1);
        publishFlow.setPublishFlowName("Test Flow");

        // Mocking the modelMapper to map PublishFlowDTO to PublishFlow
        when(modelMapper.map(publishFlowDTO, PublishFlow.class)).thenReturn(publishFlow);

        // Mocking the save method of publishFlowRepository (non-void method)
        when(publishFlowRepository.save(publishFlow)).thenReturn(publishFlow);

        // Act
        String result = taskService.createPublishFlow(publishFlowDTO);

        // Assert
        assertEquals("Publish flow created or updated successfully", result);

        // Verify the interactions with the mocks
        verify(modelMapper, times(1)).map(publishFlowDTO, PublishFlow.class);
        verify(publishFlowRepository, times(1)).save(publishFlow);
    }

    @Test
    public void testGetPublishFlowById() {
        // Arrange
        int publishFlowId = 1;

        PublishFlow publishFlow = new PublishFlow();
        publishFlow.setPublishFlowId(publishFlowId);
        publishFlow.setPublishFlowName("Sample Flow");

        PublishFlowDTO expectedDTO = new PublishFlowDTO();
        expectedDTO.setPublishFlowId(publishFlowId);
        expectedDTO.setPublishFlowName("Sample Flow");

        when(publishFlowRepository.findById(publishFlowId)).thenReturn(Optional.of(publishFlow));
        when(modelMapper.map(publishFlow, PublishFlowDTO.class)).thenReturn(expectedDTO);

        // Act
        PublishFlowDTO result = taskService.getPublishFlowById(publishFlowId);

        // Assert
        assertNotNull(result);
        assertEquals(publishFlowId, result.getPublishFlowId());
        assertEquals("Sample Flow", result.getPublishFlowName());

        verify(publishFlowRepository, times(1)).findById(publishFlowId);
        verify(modelMapper, times(1)).map(publishFlow, PublishFlowDTO.class);
    }

    @Test
    public void testUpdatePublishFlow() {
        // Arrange
        PublishFlowDTO publishFlowDTO = new PublishFlowDTO();
        publishFlowDTO.setPublishFlowId(1);
        publishFlowDTO.setPublishFlowName("Updated Flow");

        PublishFlow publishFlow = new PublishFlow();
        publishFlow.setPublishFlowId(1);
        publishFlow.setPublishFlowName("Updated Flow");

        // Mock the mapping and repository behavior
        when(modelMapper.map(eq(publishFlowDTO), any(Type.class))).thenReturn(publishFlow);

        // Act
        String result = taskService.updatePublishFlow(publishFlowDTO);

        // Assert
        assertEquals("Publish flow updated successfully", result);
        verify(modelMapper, times(1)).map(eq(publishFlowDTO), any(Type.class));
        verify(publishFlowRepository, times(1)).save(publishFlow);
    }

    @Test
    public void testDeletePublishFlow() {
        // Arrange
        int publishFlowId = 1;

        // No need to mock anything for void deleteById

        // Act
        taskService.deletePublishFlow(publishFlowId);

        // Assert
        verify(publishFlowRepository, times(1)).deleteById(publishFlowId);
    }

    @Test
    public void testFindPublishFlowByProjectId_ReturnsValidDTO() {
        // Arrange
        int projectId = 123;

        PublishFlow mockFlow = new PublishFlow();
        mockFlow.setProjectId(projectId);
        mockFlow.setPublishFlowName("Test Flow");

        PublishFlowDTO expectedDTO = new PublishFlowDTO();
        expectedDTO.setProjectId(projectId);
        expectedDTO.setPublishFlowName("Test Flow");

        List<PublishFlow> mockList = Collections.singletonList(mockFlow);

        when(publishFlowRepository.findPublishFlowsByProjectId(projectId)).thenReturn(mockList);
        when(modelMapper.map(mockFlow, PublishFlowDTO.class)).thenReturn(expectedDTO);

        // Act
        PublishFlowDTO result = taskService.findPublishFlowByProjectId(projectId);

        // Assert
        assertNotNull(result);
        assertEquals(projectId, result.getProjectId());
        assertEquals("Test Flow", result.getPublishFlowName());

        verify(publishFlowRepository, times(1)).findPublishFlowsByProjectId(projectId);
        verify(modelMapper, times(1)).map(mockFlow, PublishFlowDTO.class);
    }

    @Test
    public void testFindPublishFlowByRuleId_ReturnsValidDTO() {
        // Arrange
        int ruleId = 101;

        PublishFlow mockFlow = new PublishFlow();
        mockFlow.setPublishFlowId(1);
        mockFlow.setPublishFlowName("Rule Flow");

        PublishFlowDTO expectedDTO = new PublishFlowDTO();
        expectedDTO.setPublishFlowId(1);
        expectedDTO.setPublishFlowName("Rule Flow");

        when(publishFlowRepository.findPublishFlowByRuleId(ruleId)).thenReturn(mockFlow);
        when(modelMapper.map(mockFlow, PublishFlowDTO.class)).thenReturn(expectedDTO);

        // Act
        PublishFlowDTO result = taskService.findPublishFlowByRuleId(ruleId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getPublishFlowId());
        assertEquals("Rule Flow", result.getPublishFlowName());

        verify(publishFlowRepository, times(1)).findPublishFlowByRuleId(ruleId);
        verify(modelMapper, times(1)).map(mockFlow, PublishFlowDTO.class);
    }

    @Test
    public void testDeletePublishFlowByRuleId() {
        // Arrange
        int ruleId = 123;

        // Act
        taskService.deletePublishFlowByRuleId(ruleId);

        // Assert
        verify(publishFlowRepository, times(1)).deletePublishFlowByRuleId(ruleId);
    }
































}