package edu.IIT.task_management.service;

import edu.IIT.task_management.dto.TaskDTO;
import edu.IIT.task_management.model.Task;
import edu.IIT.task_management.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;

    @Override
    public String createTask(TaskDTO taskDTO) {
        taskRepository.save(modelMapper.map(taskDTO, Task.class));
        return "Task created successfully";
    }

    @Override
    public TaskDTO getTaskById(int id) {
        return modelMapper.map(taskRepository.findById(id), TaskDTO.class);
    }

    @Override
    public String updateTask(TaskDTO taskDTO) {
        Optional<Task> task = (taskRepository.findById(taskDTO.getTaskId()));
        if (task.isEmpty()) {
            return "Task not found";
        }
        taskDTO.setCreatedAt(task.get().getCreatedAt());
        taskRepository.save(modelMapper.map(taskDTO, new TypeToken<Task>(){}.getType()));
        return "Task updated successfully";
    }

    @Override
    public void deleteTask(int id) {
        taskRepository.deleteById(id);
    }

    @Override
    public List<TaskDTO> getAllTasks() {
        return modelMapper.map(taskRepository.findAll(), new TypeToken<List<TaskDTO>>(){}.getType());
    }

}
