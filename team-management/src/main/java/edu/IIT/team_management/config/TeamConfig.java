package edu.IIT.team_management.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TeamConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
