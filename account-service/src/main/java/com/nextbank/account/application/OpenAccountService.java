package com.nextbank.account.application;

import com.nextbank.account.application.port.in.OpenAccountUseCase;
import com.nextbank.account.application.port.out.AccountRepositoryPort;
import com.nextbank.account.application.port.out.DomainEventPublisherPort;
import com.nextbank.account.application.port.out.DomainEventPublisherPort.DomainEvent;

import com.nextbank.account.domain.Account;
import com.nextbank.account.domain.CustomerId;
import com.nextbank.account.domain.Iban;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class OpenAccountService implements OpenAccountUseCase {

    private static final Logger log = LoggerFactory.getLogger(OpenAccountService.class);

    private final AccountRepositoryPort accountRepository;
    private final DomainEventPublisherPort eventPublisher;

    public OpenAccountService(AccountRepositoryPort accountRepository, DomainEventPublisherPort eventPublishe) {
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublishe;
    }


    @Override
    @Transactional
    public Account open(OpenAccountCommand command) {
        log.info("Opening account for customer {}", command.customerId());
        Account account = Account.open(
                new CustomerId(command.customerId()),
                new Iban(command.iban()),
                command.currency()
        );

        Account saved = accountRepository.save(account);

        // Misma transacción que el guardado de arriba: esto ES el Outbox Pattern (ADR-0004).
        // El payload sigue exactamente docs/events/catalog.md -> account.account-opened.v1
        eventPublisher.publish(new DomainEvent(
                "account.account-opened.v1",
                saved.getAccountId().toString(),
                "Account",
                Map.of(
                        "accountId", saved.getAccountId().toString(),
                        "customerId", saved.getCustomerId().toString(),
                        "iban", saved.getIban().toString(),
                        "currency", saved.getCurrency().getCurrencyCode(),
                        "openedAt",  saved.getOpenedAt()
                )
        ));

        return saved;
    }
}
