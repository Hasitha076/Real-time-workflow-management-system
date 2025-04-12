package edu.IIT.task_management.repository;

import edu.IIT.task_management.dto.RuleDTO;
import edu.IIT.task_management.model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleRepository extends JpaRepository<Rule, Integer> {

    public List<Rule> findRulesByProjectId(int projectId);
}
