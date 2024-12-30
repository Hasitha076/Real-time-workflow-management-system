package edu.IIT.work_management.service;

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

    @Override
    public String createWork(WorkDTO workDTO) {
        workRepository.save(modelMapper.map(workDTO, Work.class));
//        workProducer.sendCreateTaskMessage(workDTO.getWorkName(), workDTO.getAssignerId(), workDTO.getCollaboratorIds());
        return "Work created successfully";
    }

    @Override
    public WorkDTO getWorkById(int id) {
        return modelMapper.map(workRepository.findById(id), WorkDTO.class);
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

}
