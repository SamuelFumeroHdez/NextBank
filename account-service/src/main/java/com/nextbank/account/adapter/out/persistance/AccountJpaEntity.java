package com.nextbank.account.adapter.out.persistance;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "accounts")
@Getter
public class AccountJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "iban", nullable = false, unique = true)
    private String iban;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected AccountJpaEntity() {

    }

    public AccountJpaEntity(String id, String customerId, String iban, String currency, BigDecimal balance,
                            Instant openedAt,  long version) {

        this.id = id;
        this.customerId = customerId;
        this.iban = iban;
        this.currency = currency;
        this.balance = balance;
        this.openedAt = openedAt;
        this.version = version;
    }

    public void updateBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
