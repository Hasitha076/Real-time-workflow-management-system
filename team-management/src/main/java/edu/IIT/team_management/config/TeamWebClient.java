package edu.IIT.team_management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TeamWebClient {

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean WebClient userWebClient() {

        return WebClient.builder().baseUrl("http://localhost:8081/api/v1/user").build();
    }

}
