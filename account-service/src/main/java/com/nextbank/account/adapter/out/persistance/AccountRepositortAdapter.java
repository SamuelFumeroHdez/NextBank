package com.nextbank.account.adapter.out.persistance;

import com.nextbank.account.application.port.out.AccountRepositoryPort;
import com.nextbank.account.domain.Account;
import com.nextbank.account.domain.AccountId;
import com.nextbank.account.domain.CustomerId;
import com.nextbank.account.domain.Iban;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.Optional;

@Component
public class AccountRepositortAdapter implements AccountRepositoryPort {

    private final AccountJpaRepository jpaRepository;

    public AccountRepositortAdapter(AccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Account save(Account account) {
        AccountJpaEntity entity = new AccountJpaEntity(
                account.getAccountId().toString(),
                account.getCustomerId().toString(),
                account.getIban().toString(),
                account.getCurrency().getCurrencyCode(),
                account.getBalance(),
                account.getOpenedAt(),
                account.getVersion()
        );

        jpaRepository.save(entity);

        return account;
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return jpaRepository.findById(id.toString()).map(this::toDomain);
    }

    private Account toDomain(AccountJpaEntity entity) {
        return Account.reconstitute(
                AccountId.of(entity.getId()),
                new CustomerId(entity.getCustomerId()),
                new Iban(entity.getIban()),
                Currency.getInstance(entity.getCurrency()),
                entity.getBalance(),
                entity.getOpenedAt(),
                entity.getVersion()
        );
    }
}
