package edu.IIT.notification_management.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class NotificationTopicConfig {

    @Bean
    public NewTopic userTopic() {
        return TopicBuilder.name("project-events")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
