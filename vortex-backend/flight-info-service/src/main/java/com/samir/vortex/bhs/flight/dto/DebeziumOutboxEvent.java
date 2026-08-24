package com.samir.vortex.bhs.flight.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DebeziumOutboxEvent(
        @JsonProperty("after") OutboxPayload after
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OutboxPayload(
            String id,
            @JsonProperty("event_type") String eventType,
            String payload
    ) {}
}