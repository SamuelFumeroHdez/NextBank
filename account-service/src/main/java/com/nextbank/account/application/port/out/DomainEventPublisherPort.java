package com.nextbank.account.application.port.out;

import com.nextbank.account.domain.Account;

import java.util.Map;

public interface DomainEventPublisherPort {

    void publish(DomainEvent domainEvent);
    record DomainEvent(String eventTpe, String aggregateId, String aggregateType, Map<String, Object> payload){}
}
