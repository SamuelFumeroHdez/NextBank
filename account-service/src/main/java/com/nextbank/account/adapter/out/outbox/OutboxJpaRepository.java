package com.nextbank.account.adapter.out.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxJpaRepository extends JpaRepository<OutboxEventEntity, String> {
}
