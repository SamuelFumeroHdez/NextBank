package com.nextbank.account.adapter.out.outbox;

import com.nextbank.account.adapter.in.rest.CorrelationContext;
import com.nextbank.account.application.port.out.DomainEventPublisherPort;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Component
public class OutboxEventPublisherAdapter implements DomainEventPublisherPort {

    private final OutboxJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisherAdapter(OutboxJpaRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }


    @Override
    public void publish(DomainEvent domainEvent) {

        try{
            String payloadJson = objectMapper.writeValueAsString(domainEvent.payload());
            OutboxEventEntity entity = new OutboxEventEntity(
                    UUID.randomUUID().toString(),
                    CorrelationContext.getOrGenerate(),
                    null,
                    domainEvent.eventTpe(),
                    domainEvent.aggregateId(),
                    domainEvent.aggregateType(),
                    payloadJson,
                    Instant.now()

            );
            outboxRepository.save(entity);
        }catch (Exception e){
            throw new IllegalStateException("Failed to serialize domain event payload", e);
        }

    }
}
