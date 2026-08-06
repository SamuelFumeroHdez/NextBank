package com.nextbank.account.adapter.out.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Component
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private final OutboxJooqRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final int batchSize;


    public OutboxPoller(OutboxJooqRepository outboxRepository,
                        KafkaTemplate<String, String> kafkaTemplate,
                        @Value("${nextbank.outbox.batch-size:100}") int batchSize) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${nextbank.outbox.poll-interval-ms:2000}")
    @Transactional
    public void publisPendingEvents(){
        List<OutboxJooqRepository.PendingEvent> pending = outboxRepository.findUnpublishedBatch(batchSize);
        if (pending.isEmpty()){
            return;
        }

        log.debug("Found {} pending outbox events", pending.size());
        List<String> publishedIds = new ArrayList<>(pending.size());
        for (OutboxJooqRepository.PendingEvent event : pending) {
            MDC.put("correlationId", event.correlationId());
            try{
                var message = MessageBuilder
                        .withPayload(event.payload())
                        .setHeader(KafkaHeaders.TOPIC, topicFor(event.eventType()))
                        .setHeader(KafkaHeaders.KEY, event.aggregateId())
                        .setHeader("X-Correlation-Id", event.correlationId())
                        .setHeader("X-Event-Id", event.eventId())
                        .setHeader("X-Event-Type", event.eventType())
                        .build();

                kafkaTemplate.send(message).get();

                publishedIds.add(event.eventId());
                log.info("Published event {} of type {}", event.eventId(), event.eventType() );
            }catch (Exception e){
                log.error("Failed to publish event {}. Rolling back batch, will retry.",
                        event.eventId(), e);
                throw new IllegalStateException("Outbox publication failed", e);
            } finally {
                MDC.remove("correlationId");
            }

        }
        outboxRepository.markAsPublished(publishedIds);
    }

    private String topicFor(String eventType){
        return eventType.substring(0, eventType.indexOf('.')) + "events";
    }
}
