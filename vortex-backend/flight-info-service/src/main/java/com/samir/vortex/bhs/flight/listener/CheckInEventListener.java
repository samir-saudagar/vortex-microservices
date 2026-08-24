package com.samir.vortex.bhs.flight.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.samir.vortex.bhs.flight.avro.FlightStatusVerified;
import com.samir.vortex.bhs.flight.client.FlightApiClient;
import com.samir.vortex.bhs.flight.dto.DebeziumOutboxEvent;
import com.samir.vortex.bhs.flight.dto.ExternalFlightResponse;
import com.samir.vortex.bhs.flight.exception.FlightProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class CheckInEventListener {

//    @PostConstruct
//    public void verifyStartup() {
//        log.info("🔥 SPRING WOKE UP THE KAFKA LISTENER! WAITING FOR BAGS...");
//    }
    private final JsonMapper jsonMapper;
    private final FlightApiClient flightApiClient;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, FlightStatusVerified> flightStatusKafkaTemplate;

    CheckInEventListener(@Qualifier("JsonMapper") JsonMapper jsonMapper,
                         FlightApiClient flightApiClient,
                         StringRedisTemplate redisTemplate,
                         KafkaTemplate<String, FlightStatusVerified> flightStatusKafkaTemplate) {
        this.jsonMapper = jsonMapper;
        this.flightApiClient = flightApiClient;
        this.redisTemplate = redisTemplate;
        this.flightStatusKafkaTemplate = flightStatusKafkaTemplate;
    }

    @KafkaListener(topics = "vortex.public.outbox_events", groupId = "flight-info-group")
    public void consumeOutboxEvent(String message) {
        try {
            // 1. Unpack the Debezium FedEx box
            DebeziumOutboxEvent event = jsonMapper.readValue(message, DebeziumOutboxEvent.class);

            // If it's empty or not a bag check-in, ignore it.
            if (event.after() == null || !"BAG_RECEIVED".equals(event.after().eventType())) {
                return;
            }

            // 2. Redis Idempotency Check (Duplicate prevention)
            String eventId = event.after().id();
            String idempotencyKey = "idempotency:event:" + eventId;

            // setIfAbsent returns TRUE if the key is new, FALSE if it already exists
            Boolean isNewEvent = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "PROCESSED", Duration.ofDays(1));

            if (Boolean.FALSE.equals(isNewEvent)) {
                log.info("Idempotency hit: Already processed event ID {}. Skipping.", eventId);
                return;
            }

            // 3. Extract the inner letter (the actual bag data)
            String innerPayload = event.after().payload();
            JsonNode bagData = jsonMapper.readTree(innerPayload);
            String bagTag = bagData.get("bagTag").asText();
            String flightNumber = bagData.get("flightNumber").asText();

            log.info("Kafka consumed check-in! BagTag: {}, Flight: {}", bagTag, flightNumber);

            // 4. Call our new Circuit Breaker client!
            ExternalFlightResponse flightStatus = flightApiClient.verifyFlight(flightNumber);

            //if (true) throw new RuntimeException("BOOM! Forcing a DLT test!");



            log.info("Flight Status for {} is: {}", flightNumber, flightStatus.status());

            FlightStatusVerified statusEvent = FlightStatusVerified.newBuilder()
                    .setBagTag(bagTag)
                    .setFlightNumber(flightNumber)
                    .setFlightStatus(flightStatus.status())
                    .setVerifiedAt(Instant.now())
                    .build();

            flightStatusKafkaTemplate.send("vortex.flight.verified", bagTag, statusEvent);
            log.info("Published FlightStatusVerified event to Kafka for bagTag: {}", bagTag);

        } catch (Exception e) {
            log.error("Failed to process outbox event: {}", message, e);
            throw new FlightProcessingException("Failed to process flight data from Kafka outbox event", e);
        }
    }
}