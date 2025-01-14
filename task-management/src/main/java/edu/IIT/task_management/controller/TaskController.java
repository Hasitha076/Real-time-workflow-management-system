package edu.IIT.task_management.controller;

import edu.IIT.task_management.dto.TaskDTO;
import edu.IIT.task_management.producer.TaskProducer;
import edu.IIT.task_management.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/task")
@RequiredArgsConstructor
@CrossOrigin
public class TaskController {

    private final TaskService taskService;
    private final TaskProducer taskProducer;

    @PostMapping("/createTask")
    public String addTask(@RequestBody TaskDTO taskDTO) {
    taskProducer.sendMessage(taskDTO);
        return taskService.createTask(taskDTO);
    }

    @PutMapping("/updateTask")
    public String updateTask(@RequestBody TaskDTO taskDTO) {
        return taskService.updateTask(taskDTO);
    }

    @DeleteMapping("/deleteTask/{id}")
    public String deleteTask(@PathVariable int id) {
        taskService.deleteTask(id);
        return "Task deleted successfully";
    }

    @GetMapping("/getTask/{id}")
    public TaskDTO getTask(@PathVariable int id) {
//        taskProducer.sendMessage(taskService.getTaskById(id));
        return taskService.getTaskById(id);
    }

    @GetMapping("/getTasksByProjectId/{id}")
    public List<TaskDTO> getTasksByProjectId(@PathVariable int id) {
        return taskService.getTasksByProjectId(id);
    }

    @GetMapping("/getTasksByWorkId/{id}")
    public List<TaskDTO> getTasksByWorkId(@PathVariable int id) {
        return taskService.getTasksByWorkId(id);
    }

    @GetMapping("/getAllTasks")
    public List<TaskDTO> getAllTasks() {
        return taskService.getAllTasks();
    }

    @PutMapping("/changeTaskStatus/{taskId}")
    public void changeTaskStatus(@PathVariable int taskId, @RequestBody boolean status) {
        taskService.changeTaskStatus(taskId, status);
    }

}
