package edu.IIT.task_management.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class TaskTopicConfig {

    @Bean
    public NewTopic userTopic() {
        return TopicBuilder.name("task-events")
                .build();
    }
}
