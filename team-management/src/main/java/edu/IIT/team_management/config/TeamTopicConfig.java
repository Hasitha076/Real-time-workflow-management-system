package edu.IIT.team_management.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class TeamTopicConfig {

    @Bean
    public NewTopic userTopic() {
        return TopicBuilder.name("team-events")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
