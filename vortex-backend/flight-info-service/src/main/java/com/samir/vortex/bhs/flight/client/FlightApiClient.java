package com.samir.vortex.bhs.flight.client;

import com.samir.vortex.bhs.flight.dto.ExternalFlightResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Slf4j
@Component
public class FlightApiClient {

    private final RestClient restClient;
    private final StringRedisTemplate redisTemplate;

    private static final String REDIS_PREFIX = "flight:status:";

    public FlightApiClient(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:9999/api/v1/mock-airline")
                .build();
    }

    @Retry(name = "flightApi")
    @CircuitBreaker(name = "flightApi", fallbackMethod = "fallbackFlightInfo")
    public ExternalFlightResponse verifyFlight(String flightNumber) {
        log.info("Calling external API to verify flight: {}", flightNumber);

        ExternalFlightResponse response = restClient.get()
                .uri("/flights/{flightNumber}", flightNumber)
                .retrieve()
                .body(ExternalFlightResponse.class);

        if (response != null && response.status() != null) {
            redisTemplate.opsForValue().set(
                    REDIS_PREFIX + flightNumber,
                    response.status(),
                    Duration.ofHours(12)
            );
            log.info("Cached flight status for {} in Redis.", flightNumber);
        }

        return response;
    }

    public ExternalFlightResponse fallbackFlightInfo(String flightNumber, Throwable t) {
        log.warn("External API down! Circuit Breaker triggered. Reason: {}", t.getMessage());
        log.info("Attempting to rescue flight {} from Redis Cache...", flightNumber);

        String cachedStatus = redisTemplate.opsForValue().get(REDIS_PREFIX + flightNumber);

        if (cachedStatus != null) {
            log.info("Rescue successful! Found {} in Redis with status: {}", flightNumber, cachedStatus);
            return new ExternalFlightResponse(flightNumber, cachedStatus, null);
        }

        log.warn("Rescue failed. No cached data for {}. Returning UNKNOWN.", flightNumber);
        return new ExternalFlightResponse(flightNumber, "UNKNOWN", null);
    }
}