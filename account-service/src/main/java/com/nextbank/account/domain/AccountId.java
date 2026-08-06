package com.nextbank.account.domain;

import java.util.Objects;
import java.util.UUID;

public record AccountId(String value) {

    public AccountId{
        Objects.requireNonNull(value);
    }

    public static AccountId newId(){
        return new AccountId("acc_" + UUID.randomUUID());
    }

    public static AccountId of(String value){
        return new AccountId(value);
    }

    @Override
    public String toString() {
        return value;
    }

}
