package edu.IIT.task_management.producer;

import edu.IIT.task_management.dto.TaskDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskProducer {

    private final KafkaTemplate<TaskDTO, TaskDTO> kafkaTemplate;

    public void sendMessage(TaskDTO taskDTO) {
        log.info(String.format("#### -> Producing message -> %s", taskDTO));
        kafkaTemplate.send("task-events", taskDTO);
    }
}
