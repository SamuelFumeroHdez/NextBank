package com.nextbank.account.domain;

import java.util.Objects;

public record CustomerId(String value) {
    public CustomerId{
        Objects.requireNonNull(value);
    }

    @Override
    public String toString(){
        return value;
    }
}
