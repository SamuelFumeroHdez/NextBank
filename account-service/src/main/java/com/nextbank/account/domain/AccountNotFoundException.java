package com.nextbank.account.domain;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String accountId) {
        super("Account not found: " +  accountId);
    }
}
