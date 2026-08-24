package com.samir.vortex.bhs.checkin.dto;

import com.samir.vortex.bhs.checkin.model.BagStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record BagCheckInResponse (
        UUID id,
        String bagTag,
        String flightNumber,
        BagStatus status,
        Instant createdAt
) {}