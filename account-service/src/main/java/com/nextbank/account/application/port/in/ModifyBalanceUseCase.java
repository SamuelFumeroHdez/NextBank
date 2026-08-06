package com.nextbank.account.application.port.in;

import com.nextbank.account.domain.Account;

import java.math.BigDecimal;

public interface ModifyBalanceUseCase {

    Account debit(BalanceCommand command);

    Account credit(BalanceCommand command);

    record BalanceCommand(String acocuntId, BigDecimal ammount, String idempotencyKey){}
}
