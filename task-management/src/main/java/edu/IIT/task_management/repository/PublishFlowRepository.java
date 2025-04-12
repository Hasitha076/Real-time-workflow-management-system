package edu.IIT.task_management.repository;

import edu.IIT.task_management.model.PublishFlow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublishFlowRepository extends JpaRepository<PublishFlow, Integer> {
    PublishFlow findPublishFlowByProjectId(int projectId);
}
