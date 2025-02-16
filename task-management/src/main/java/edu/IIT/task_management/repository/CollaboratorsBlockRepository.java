package edu.IIT.task_management.repository;

import edu.IIT.task_management.model.CollaboratorsBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollaboratorsBlockRepository extends JpaRepository<CollaboratorsBlock, Integer> {
    public CollaboratorsBlock findByWorkId(int workId);
}
