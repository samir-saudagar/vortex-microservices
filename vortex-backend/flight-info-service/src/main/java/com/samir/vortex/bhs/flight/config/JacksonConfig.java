package com.samir.vortex.bhs.flight.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

   @Bean( name = "JsonMapper")
   public JsonMapper jsonMapper() {
      return JsonMapper.builder()
              .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
              .addModule(new JavaTimeModule())
              .build();
   }
}