package com.nextbank.account.application.port.in;

import com.nextbank.account.domain.Account;

import java.util.Currency;

public interface OpenAccountUseCase {

    Account open(OpenAccountCommand command);

    record OpenAccountCommand(String customerId, String iban, Currency currency){}
}
