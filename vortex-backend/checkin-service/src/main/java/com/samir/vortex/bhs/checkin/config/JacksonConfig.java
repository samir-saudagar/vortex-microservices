package com.samir.vortex.bhs.checkin.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /**
     * Dedicated JsonMapper for internal event serialization (Outbox/Kafka).
     * Isolated from Spring's default Web ObjectMapper to prevent API changes from breaking Kafka contracts.
     */
    @Bean(name = "JsonMapper")
    public JsonMapper JsonMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule()) // Handles Instant/UUIDs
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // Strict ISO-8601 formatting
                .build();
    }
}