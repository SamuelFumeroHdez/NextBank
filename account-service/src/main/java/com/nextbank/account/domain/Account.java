package com.nextbank.account.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Objects;

@Getter
public class Account {

    private final AccountId accountId;
    private final CustomerId customerId;
    private final Iban iban;
    private final Currency currency;
    private BigDecimal balance;
    private final Instant openedAt;
    private final long version;


    public Account(AccountId accountId, CustomerId customerId, Iban iban, Currency currency, BigDecimal balance,
                   Instant openedAt, long version) {

        this.accountId = Objects.requireNonNull(accountId);
        this.customerId = Objects.requireNonNull(customerId);
        this.iban = Objects.requireNonNull(iban);
        this.currency = Objects.requireNonNull(currency);
        this.balance = Objects.requireNonNull(balance);
        this.openedAt = Objects.requireNonNull(openedAt);
        this.version = version;
    }

    public static Account open(CustomerId customerId, Iban iban, Currency currency){
        return new Account(AccountId.newId(), customerId, iban, currency, BigDecimal.ZERO, Instant.now(), 0L);
    }

    public static Account reconstitute(AccountId accountId, CustomerId customerId, Iban iban,
                                       Currency currency, BigDecimal balance, Instant openedAt, long version) {

        return new Account(accountId, customerId, iban, currency, balance, openedAt, version);
    }

    public void credit(BigDecimal amount){
        requirePositive(amount);
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount){
        requirePositive(amount);
        if(this.balance.subtract(amount).compareTo(BigDecimal.ZERO) < 0){
            throw new InsufficientFundsException(accountId, amount, balance);
        }
        this.balance = this.balance.subtract(amount);
    }

    private void requirePositive(BigDecimal amount){
        if(amount.signum() <= 0){
            throw new IllegalArgumentException("Amount must be positive");
        }
    }
}
