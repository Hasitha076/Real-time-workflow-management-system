package edu.IIT.task_management.repository;

import edu.IIT.task_management.dto.TaskDTO;
import edu.IIT.task_management.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    public List<Task> findByProjectId(int projectId);

    public List<Task> findByWorkId(int workId);
}
