package com.samir.vortex.bhs.flight.dto;

public record ExternalFlightResponse(
        String flightNumber,
        String status,
        String routingCode
) {}