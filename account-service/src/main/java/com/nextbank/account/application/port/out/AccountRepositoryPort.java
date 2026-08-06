package com.nextbank.account.application.port.out;

import com.nextbank.account.domain.Account;
import com.nextbank.account.domain.AccountId;

import java.util.Optional;

public interface AccountRepositoryPort {

    Account save(Account account);
    Optional<Account> findById(AccountId id);
}
