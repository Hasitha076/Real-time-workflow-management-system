package edu.IIT.task_management.repository;

import edu.IIT.task_management.model.PublishFlow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublishFlowRepository extends JpaRepository<PublishFlow, Integer> {
    List<PublishFlow> findPublishFlowsByProjectId(int projectId);
    PublishFlow findPublishFlowByRuleId(int ruleId);
    void deletePublishFlowByRuleId(int ruleId);
}
