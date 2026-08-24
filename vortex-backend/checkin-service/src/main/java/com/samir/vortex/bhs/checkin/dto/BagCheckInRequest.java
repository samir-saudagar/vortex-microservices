package com.samir.vortex.bhs.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record BagCheckInRequest (
        @NotBlank(message = "Bag tag is required")
        String bagTag,

        @NotBlank(message = "Flight number is required")
        String flightNumber
) {}