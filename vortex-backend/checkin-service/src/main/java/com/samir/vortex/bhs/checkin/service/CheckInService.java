package com.samir.vortex.bhs.checkin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.samir.vortex.bhs.checkin.audit.AuditEventPublisher;
import com.samir.vortex.bhs.checkin.dto.BagCheckInRequest;
import com.samir.vortex.bhs.checkin.dto.BagCheckInResponse;
import com.samir.vortex.bhs.checkin.exception.BagNotFoundException;
import com.samir.vortex.bhs.checkin.exception.DuplicateBagException;
import com.samir.vortex.bhs.checkin.mapper.BagMapper;
import com.samir.vortex.bhs.checkin.model.Bag;
import com.samir.vortex.bhs.checkin.model.OutboxEvent;
import com.samir.vortex.bhs.checkin.repository.BagRepository;
import com.samir.vortex.bhs.checkin.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class CheckInService {

    private static final String AGGREGATE_TYPE_BAG = "BAG";
    private static final String EVENT_TYPE_BAG_RECEIVED = "BAG_RECEIVED";

    private final BagRepository bagRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper jsonMapper;
    private final BagMapper bagMapper;
    private final AuditEventPublisher auditEventPublisher;

    public CheckInService(BagRepository bagRepository,
                          OutboxEventRepository outboxEventRepository,
                          @Qualifier("JsonMapper") JsonMapper jsonMapper,
                          BagMapper bagMapper,
                          AuditEventPublisher auditEventPublisher) {
        this.bagRepository = bagRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.jsonMapper = jsonMapper;
        this.bagMapper = bagMapper;
        this.auditEventPublisher = auditEventPublisher;
    }

    @Transactional
    public BagCheckInResponse processCheckIn(BagCheckInRequest bagCheckInRequest) {
        log.info("Initiating check-in process for bagTag: {} on flight: {}", bagCheckInRequest.bagTag(), bagCheckInRequest.flightNumber());

        if (bagRepository.existsByBagTag(bagCheckInRequest.bagTag())) {
            log.warn("Check-in rejected: BagTag {} already exists.", bagCheckInRequest.bagTag());
            throw new DuplicateBagException("Bag with tag " + bagCheckInRequest.bagTag() + " is already checked in.");
        }

        Bag bag = bagMapper.toEntity(bagCheckInRequest);
        Bag savedBag = bagRepository.saveAndFlush(bag);

        log.debug("Bag entity saved successfully with ID: {}", savedBag.getId());

        // 1. Flow A: Save to Outbox (For Debezium/Avro)
        saveOutboxEvent(savedBag);

        // 2. Flow B: Fire Audit Event directly to Kafka
        auditEventPublisher.publish(
                "BAG_CHECKED_IN",
                "CheckInService",
                "Bag " + bagCheckInRequest.bagTag() + " successfully checked in for flight " + bagCheckInRequest.flightNumber()
        );

        log.info("Successfully checked in bagTag: {} with ID: {}", savedBag.getBagTag(), savedBag.getId());
        return bagMapper.toResponse(savedBag);
    }

    public BagCheckInResponse getBagById(UUID id) {
        log.debug("Fetching bag with ID: {}", id);

        Bag bag = bagRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Bag lookup failed: No bag found with ID: {}", id);
                    return new BagNotFoundException("Bag not found with id: " + id);
                });

        return bagMapper.toResponse(bag);
    }

    public Page<BagCheckInResponse> getAllBags(Pageable pageable) {
        log.debug("Fetching paginated bags: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        return bagRepository.findAll(pageable)
                .map(bagMapper::toResponse);
    }

    private void saveOutboxEvent(Bag bag) {
        try {
            String jsonPayload = jsonMapper.writeValueAsString(bag);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType(AGGREGATE_TYPE_BAG)
                    .aggregateId(bag.getId())
                    .eventType(EVENT_TYPE_BAG_RECEIVED)
                    .payload(jsonPayload)
                    .processed(false)
                    .build();

            outboxEventRepository.save(outboxEvent);
            log.debug("Outbox event recorded for bag ID: {} with event: {}", bag.getId(), EVENT_TYPE_BAG_RECEIVED);

        } catch (JsonProcessingException e) {
            log.error("CRITICAL: Failed to serialize Outbox payload for bag ID: {}. Transaction will rollback.", bag.getId(), e);
            throw new RuntimeException("Failed to serialize outbox event payload for bag ID: " + bag.getId(), e);
        }
    }
}