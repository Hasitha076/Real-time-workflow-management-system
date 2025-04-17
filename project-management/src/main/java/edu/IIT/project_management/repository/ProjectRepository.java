package edu.IIT.project_management.repository;

import edu.IIT.project_management.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {

    public List<Project> findByTeamIds(int teamId);
    public List<Project> findByCollaboratorIds(int collaboratorId);
}
