package com.samir.vortex.bhs.checkin.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuditEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AuditEventPublisher.class);
    private static final String AUDIT_TOPIC = "vortex.audit.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AuditEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String action, String actor, String details) {
        AuditEvent event = AuditEvent.create("checkin-service", action, actor, details);
        try {
            kafkaTemplate.send(AUDIT_TOPIC, event.eventId(), event);
            log.info("Published audit event [{}] for action [{}]", event.eventId(), action);
        } catch (Exception e) {
            log.error("Failed to publish audit event [{}]", event.eventId(), e);
        }
    }
}