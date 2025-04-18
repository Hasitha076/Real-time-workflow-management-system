package edu.IIT.task_management;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.IIT.task_management.controller.TaskController;
import edu.IIT.task_management.dto.TaskDTO;
import edu.IIT.task_management.dto.TaskPriorityLevel;
import edu.IIT.task_management.service.TaskService;
import edu.IIT.task_management.producer.TaskProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @Mock
    private TaskProducer taskProducer;

    @InjectMocks
    private TaskController taskController;

    @Autowired
    private ObjectMapper objectMapper;  // Injecting the ObjectMapper


    @BeforeEach
    void setUp() {
        // Set up the MockMvc object and inject mocks
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
    }

    @Test
    void testCreateTask() throws Exception {
        // Example TaskDTO to test the controller
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTaskId(1);
        taskDTO.setTaskName("Test Task");
        taskDTO.setDescription("Test Task Description");
        taskDTO.setAssignerId(101);
        taskDTO.setPriority(TaskPriorityLevel.HIGH);

        // Mock the behavior of TaskService
        when(taskService.createTask(any(TaskDTO.class))).thenReturn("Task created successfully");

        // Perform the POST request to create a task
        mockMvc.perform(post("/api/v1/task/createTask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "  \"taskId\": 1,\n" +
                                "  \"taskName\": \"Test Task\",\n" +
                                "  \"description\": \"Test Task Description\",\n" +
                                "  \"assignerId\": 101,\n" +
                                "  \"priority\": \"HIGH\"\n" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Task created successfully"));

        // Verify the service and producer were called once
        verify(taskService, times(1)).createTask(any(TaskDTO.class));
        verify(taskProducer, times(1)).sendMessage(any(TaskDTO.class));
    }


//    @Test
//    void testUpdateTask() throws Exception {
//        TaskDTO taskDTO = new TaskDTO();
//        taskDTO.setTaskId(1);
//        taskDTO.setTaskName("Updated Task");
//
//        // Mock the service's behavior
//        when(taskService.updateTask(taskDTO)).thenReturn("Task updated successfully");
//
//        mockMvc.perform(put("/api/v1/task/updateTask")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\n" +
//                                "  \"taskId\": 1,\n" +
//                                "  \"taskName\": \"Updated Task\"\n" +
//                                "}"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Task updated successfully"));
//
//        verify(taskService, times(1)).updateTask(any(TaskDTO.class));
//    }
//
//    @Test
//    void testDeleteTask() throws Exception {
//        int taskId = 1;
//
//        // Mock the service's behavior
//        doNothing().when(taskService).deleteTask(taskId);
//
//        mockMvc.perform(delete("/api/v1/task/deleteTask/{id}", taskId))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Task deleted successfully"));
//
//        verify(taskService, times(1)).deleteTask(taskId);
//    }
//
//    @Test
//    void testGetTaskById() throws Exception {
//        int taskId = 1;
//
//        // Mock the service's behavior
//        TaskDTO taskDTO = new TaskDTO();
//        taskDTO.setTaskId(taskId);
//        taskDTO.setTaskName("Test Task");
//
//        when(taskService.getTaskById(taskId)).thenReturn(taskDTO);
//
//        mockMvc.perform(get("/api/v1/task/getTask/{id}", taskId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.taskId").value(taskId))
//                .andExpect(jsonPath("$.taskName").value("Test Task"));
//
//        verify(taskService, times(1)).getTaskById(taskId);
//    }
//
//    @Test
//    void testGetAllTasks() throws Exception {
//        // Mock the service's behavior
//        TaskDTO taskDTO1 = new TaskDTO();
//        taskDTO1.setTaskId(1);
//        taskDTO1.setTaskName("Task 1");
//
//        TaskDTO taskDTO2 = new TaskDTO();
//        taskDTO2.setTaskId(2);
//        taskDTO2.setTaskName("Task 2");
//
//        when(taskService.getAllTasks()).thenReturn(List.of(taskDTO1, taskDTO2));
//
//        mockMvc.perform(get("/api/v1/task/getAllTasks"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].taskName").value("Task 1"))
//                .andExpect(jsonPath("$[1].taskName").value("Task 2"));
//
//        verify(taskService, times(1)).getAllTasks();
//    }

    // Additional tests for other endpoints can follow the same structure
}
