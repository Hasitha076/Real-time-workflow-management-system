package edu.IIT.work_management.repository;

import edu.IIT.work_management.model.Work;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkRepository extends JpaRepository<Work, Integer> {
    public List<Work> findByProjectId(int projectId);
}
