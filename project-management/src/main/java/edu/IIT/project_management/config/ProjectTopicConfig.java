package edu.IIT.project_management.config;

import lombok.*;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class ProjectTopicConfig {

    @Bean
    public NewTopic userTopic() {
        return TopicBuilder.name("project-events")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
