package edu.IIT.task_management.service;

import edu.IIT.task_management.dto.TaskDTO;

import java.util.List;

public interface TaskService {

    public String createTask(TaskDTO taskDTO);
    public TaskDTO getTaskById(int id);
    public String updateTask(TaskDTO taskDTO);
    public void deleteTask(int id);
    public List<TaskDTO> getAllTasks();
}
