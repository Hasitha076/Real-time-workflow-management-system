package edu.IIT.work_management.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class WorkTopicConfig {

    @Bean
    public NewTopic userTopic() {
        return TopicBuilder.name("work-events")
                .build();
    }
}
