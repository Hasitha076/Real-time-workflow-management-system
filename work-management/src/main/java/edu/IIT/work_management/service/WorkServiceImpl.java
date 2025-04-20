package edu.IIT.work_management.service;

import edu.IIT.work_management.dto.CollaboratorsRequest;
import edu.IIT.work_management.dto.WorkDTO;
import edu.IIT.work_management.model.Work;
import edu.IIT.work_management.producer.WorkProducer;
import edu.IIT.work_management.repository.WorkRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkServiceImpl implements WorkService {

    private final WorkRepository workRepository;
    private final ModelMapper modelMapper;
    private final WorkProducer workProducer;
    private final WebClient userWebClient;
    private final WebClient teamWebClient;

    @Override
    public String createWork(WorkDTO workDTO) {

        List<String> users = userWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterUserNames") // Ensure path matches the controller
                        .queryParam("ids", workDTO.getCollaboratorIds()) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        List<String> teams = teamWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterTeams") // Ensure path matches the controller
                        .queryParam("ids", workDTO.getTeamIds()) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        System.out.println("Users: " + users);
        System.out.println("Teams: " + teams);


        List<String> userInitials = users.stream()
                .map(name -> name.substring(0, 1).toUpperCase())
                .collect(Collectors.toList());

        List<String> teamsInitials = teams.stream()
                .map(name -> name.substring(0, 1).toUpperCase())
                .collect(Collectors.toList());

        // Combine initials
        List<String> memberIcons = new ArrayList<>();
        memberIcons.addAll(userInitials);
        memberIcons.addAll(teamsInitials);

        // Set the member icons in the project
        workDTO.setMemberIcons(memberIcons);

        workRepository.save(modelMapper.map(workDTO, Work.class));
        workProducer.sendCreateWorkMessage(workDTO.getWorkName(), workDTO.getAssignerId(), workDTO.getCollaboratorIds());
        return "Work created successfully";
    }

    @Override
    public WorkDTO getWorkById(int id) {

        Optional<Work> workOptional = workRepository.findById(id);

        if (workOptional.isPresent()) {
            return modelMapper.map(workOptional.get(), WorkDTO.class);
        } else {
            return null;
        }
    }


    @Override
    public String updateWork(WorkDTO workDTO) {
        Optional<Work> task = (workRepository.findById(workDTO.getWorkId()));
        if (task.isEmpty()) {
            return "Work not found";
        }

        // Retain the createdAt value from the old task
        workDTO.setCreatedAt(task.get().getCreatedAt());
        List<Integer> oldCollaboratorIds = task.get().getCollaboratorIds(); // Get existing collaborators

        // Identify new collaborators: present in new project but not in old task
        List<Integer> newCollaborators = workDTO.getCollaboratorIds().stream()
                .filter(collaboratorId -> !oldCollaboratorIds.contains(collaboratorId))
                .collect(Collectors.toList());

        // Identify removed collaborators: present in old project but not in new task
        List<Integer> removedCollaborators = oldCollaboratorIds.stream()
                .filter(collaboratorId -> !workDTO.getCollaboratorIds().contains(collaboratorId))
                .collect(Collectors.toList());

        // Identify unchanged collaborators: present in both old and new task
        List<Integer> unchangedCollaborators = oldCollaboratorIds.stream()
                .filter(workDTO.getCollaboratorIds()::contains)
                .collect(Collectors.toList());

        workDTO.setCreatedAt(task.get().getCreatedAt());

        List<String> users = userWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterUserNames") // Ensure path matches the controller
                        .queryParam("ids", workDTO.getCollaboratorIds()) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        List<String> teams = teamWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterTeams") // Ensure path matches the controller
                        .queryParam("ids", workDTO.getTeamIds()) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        System.out.println("Users: " + users);
        System.out.println("Teams: " + teams);


        List<String> userInitials = users.stream()
                .map(name -> name.substring(0, 1).toUpperCase())
                .collect(Collectors.toList());

        List<String> teamsInitials = teams.stream()
                .map(name -> name.substring(0, 1).toUpperCase())
                .collect(Collectors.toList());

        // Combine initials
        List<String> memberIcons = new ArrayList<>();
        memberIcons.addAll(userInitials);
        memberIcons.addAll(teamsInitials);

        // Set the member icons in the project
        workDTO.setMemberIcons(memberIcons);


        workRepository.save(modelMapper.map(workDTO, new TypeToken<Work>(){}.getType()));

        // Handle sending collaborator changes to the producer or further actions
        if (!newCollaborators.isEmpty()) {
            // Send newly added collaborators
            workProducer.sendUpdateWorkMessage(workDTO.getWorkName(), workDTO.getAssignerId(),"New", newCollaborators);
        }

        if (!removedCollaborators.isEmpty()) {
            // Send removed collaborators
            workProducer.sendUpdateWorkMessage(workDTO.getWorkName(), workDTO.getAssignerId(),"Removed", removedCollaborators);
        }

        // Optionally, you can also send unchanged collaborators if needed
        if (!unchangedCollaborators.isEmpty()) {
            // Handle unchanged collaborators if necessary
            workProducer.sendUpdateWorkMessage(workDTO.getWorkName(), workDTO.getAssignerId(),"Existing", unchangedCollaborators);
        }

        return "Work updated successfully";
    }

    @Override
    public void deleteWork(int id) {
        WorkDTO work = getWorkById(id);
        workRepository.deleteById(id);

        workProducer.sendDeleteWorkMessage(work.getProjectId(), work.getWorkName(), work.getAssignerId(), work.getCollaboratorIds());
    }

    @Transactional
    @Override
    public void deleteByProjectId(int projectId) {
        // Fetch all tasks that match the project ID
        List<Work> works = workRepository.findByProjectId(projectId);

        if (works.isEmpty()) {
            throw new ResourceNotFoundException("No work found for project ID: " + projectId);
        }

        works.forEach(work -> {
            // Delete each task
            workRepository.deleteById(work.getWorkId());

            // Send notification for deleted task
            workProducer.sendDeleteWorkMessage( work.getWorkId(), work.getWorkName(), work.getAssignerId(), work.getCollaboratorIds());
        });
    }


    @Override
    public List<WorkDTO> getAllWorks() {
        return modelMapper.map(workRepository.findAll(), new TypeToken<List<WorkDTO>>(){}.getType());
    }

    @Override
    public List<WorkDTO> getWorksByProjectId(int projectId) {
        // Fetch all tasks that match the project ID
        List<Work> works = workRepository.findByProjectId(projectId);

        if (works.isEmpty()) {
            return null;
//            throw new ResourceNotFoundException("No works found for project ID: " + projectId);
        }

        return modelMapper.map(works, new TypeToken<List<WorkDTO>>(){}.getType());
    }

    @Override
    public void updateCollaborators(int workId, CollaboratorsRequest collaboratorsRequest) {
        // Retrieve the existing project
        WorkDTO work = getWorkById(workId);

        // Get the current collaborators and teams
        List<Integer> existingCollaborators = work.getCollaboratorIds();
        List<Integer> existingTeams = work.getTeamIds();

        // If the lists are null, initialize them
        if (existingCollaborators == null) {
            existingCollaborators = new ArrayList<>();
        }
        if (existingTeams == null) {
            existingTeams = new ArrayList<>();
        }

        // Add new collaborators and teams (avoid duplicates)
        for (Integer collaboratorId : collaboratorsRequest.getCollaboratorIds()) {
            if (!existingCollaborators.contains(collaboratorId)) {
                existingCollaborators.add(collaboratorId);
            }
        }
        for (Integer teamId : collaboratorsRequest.getTeamIds()) {
            if (!existingTeams.contains(teamId)) {
                existingTeams.add(teamId);
            }
        }

        // Update the project with the new lists
        work.setCollaboratorIds(collaboratorsRequest.getCollaboratorIds());
        work.setTeamIds(collaboratorsRequest.getTeamIds());

        // Save the updated project
        updateWork(work);
    }

    @Override
    public List<WorkDTO> getWorksByTeamId(int teamId) {
        List<Work> works = workRepository.findByTeamIds(teamId);

        log.info("Works: {}", works);

        if (works.isEmpty()) {
            throw new ResourceNotFoundException("No works found for team ID: " + teamId);
        }

        return modelMapper.map(works, new TypeToken<List<WorkDTO>>(){}.getType());
    }

    @Override
    public String updateWorkStatus(int workId, Boolean status) {
        WorkDTO work = getWorkById(workId);
        System.out.println("Work: " + work);
        work.setStatus(status);
        workRepository.save(modelMapper.map(work, new TypeToken<Work>(){}.getType()));
        return "Work status updated successfully";
    }

}
