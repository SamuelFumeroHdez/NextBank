package com.nextbank.account.application;

import com.nextbank.account.application.port.in.ModifyBalanceUseCase;
import com.nextbank.account.application.port.out.AccountRepositoryPort;
import com.nextbank.account.domain.Account;
import com.nextbank.account.domain.AccountId;
import com.nextbank.account.domain.AccountNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModifyBalanceService implements ModifyBalanceUseCase {

    private final AccountRepositoryPort accountRepository;

    public ModifyBalanceService(AccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public Account debit(BalanceCommand command) {
        Account account = load(command.acocuntId());
        account.debit(command.ammount());
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public Account credit(BalanceCommand command) {
        Account account = load(command.acocuntId());
        account.credit(command.ammount());
        return accountRepository.save(account);
    }

    private Account load(String accountId) throws AccountNotFoundException {
        return accountRepository.findById(AccountId.of(accountId))
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}
