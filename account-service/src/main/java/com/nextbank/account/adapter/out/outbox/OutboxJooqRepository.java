package com.nextbank.account.adapter.out.outbox;

import org.jooq.DSLContext;
import org.jooq.Record5;
import org.jooq.Record7;
import org.jooq.Result;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static com.nextbank.account.infrastructure.jooq.Tables.OUTBOX;
import static org.jooq.impl.DSL.orderBy;


/**
 * Acceso a la tabla outbox con jOOQ.
 *
 * Se usa jOOQ y no JPA aqui porque el poller necesita SELECT ... FOR UPDATE
 * SKIP LOCKED y un UPDATE por lote: SQL que jOOQ expresa de forma directa y
 * verificada en compilacion, mientras que en JPA requiere hints opacos.
 * Ver ADR-0008.
 */

@Repository
public class OutboxJooqRepository {

    private final DSLContext dsl;


    public OutboxJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<PendingEvent> findUnpublishedBatch(int batchSize) {
        Result<Record7<String, String, String, String, String, String, Instant>> result = dsl
                .select(OUTBOX.EVENT_ID,
                        OUTBOX.CORRELATION_ID,
                        OUTBOX.CAUSATION_ID,
                        OUTBOX.EVENT_TYPE,
                        OUTBOX.AGGREGATE_ID,
                        OUTBOX.PAYLOAD,
                        OUTBOX.OCCURRED_AT)
                .from(OUTBOX)
                .where(OUTBOX.PUBLISHED.isFalse())
                .orderBy(OUTBOX.OCCURRED_AT.asc())
                .limit(batchSize)
                .forUpdate()
                .skipLocked()
                .fetch();

        return result.map(r -> new PendingEvent(
                r.get(OUTBOX.EVENT_ID),
                r.get(OUTBOX.CORRELATION_ID),
                r.get(OUTBOX.CAUSATION_ID),
                r.get(OUTBOX.EVENT_TYPE),
                r.get(OUTBOX.AGGREGATE_ID),
                r.get(OUTBOX.PAYLOAD),
                r.get(OUTBOX.OCCURRED_AT)
        ));

    }

    public void markAsPublished(List<String> eventIds) {
        if (eventIds.isEmpty()) {
            return;
        }
        int updated = dsl.update(OUTBOX)
                .set(OUTBOX.PUBLISHED, true)
                .where(OUTBOX.EVENT_ID.in(eventIds))
                .execute();
        System.out.println(">>> Filas actualizadas: " + updated + " de " + eventIds.size());
    }

    public record PendingEvent(String eventId, String correlationId, String causationId, String eventType, String aggregateId, String payload, Instant ocurredAt){}
}
