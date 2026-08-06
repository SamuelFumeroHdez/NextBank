package com.nextbank.account.domain;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException{
    public InsufficientFundsException(AccountId accountId, BigDecimal requested, BigDecimal available){
        super("Account %s has insufficient funds: requested %s, available %s"
                .formatted(accountId, requested, available));
    }
}
