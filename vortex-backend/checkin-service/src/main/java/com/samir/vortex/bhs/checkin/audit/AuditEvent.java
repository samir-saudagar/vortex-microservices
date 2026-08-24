package com.samir.vortex.bhs.checkin.audit;

import java.time.Instant;

public record AuditEvent(
        String eventId,
        String serviceName,
        String action,
        String actor,
        String details,
        Instant timestamp
) {
    public static AuditEvent create(String serviceName, String action, String actor, String details) {
        return new AuditEvent(
                java.util.UUID.randomUUID().toString(),
                serviceName,
                action,
                actor,
                details,
                Instant.now()
        );
    }
}