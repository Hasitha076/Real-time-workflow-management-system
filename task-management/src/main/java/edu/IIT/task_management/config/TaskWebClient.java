package edu.IIT.task_management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TaskWebClient {

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean WebClient userWebClient() {
        return WebClient.builder().baseUrl("http://localhost:8081/api/v1/user").build();
    }

    @Bean WebClient teamWebClient() {
        return WebClient.builder().baseUrl("http://localhost:8085/api/v1/team").build();
    }

}
