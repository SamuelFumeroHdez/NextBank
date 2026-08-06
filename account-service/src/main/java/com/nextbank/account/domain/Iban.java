package com.nextbank.account.domain;

import java.math.BigInteger;
import java.util.Objects;
import java.util.regex.Pattern;

public record Iban(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[A-Z]{2}\\d{2}[A-Z0-9]{1,30}$");

    public Iban{
        Objects.requireNonNull(value);
        String normalized = value.replace(" ", "").toUpperCase();

        if(!FORMAT.matcher(normalized).matches())
            throw new IllegalArgumentException("Invalid IBAN format: " +  normalized);

        if(!isCheckSumValid(normalized)){
            throw new IllegalArgumentException("Invalid IBAN checksum: " + normalized);
        }

        value = normalized;

    }

    private static boolean isCheckSumValid(String iban) {
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        StringBuilder numeric = new StringBuilder();

        for (char c : rearranged.toCharArray()) {
            numeric.append(Character.isLetter(c) ? (c-'A' + 10) : String.valueOf(c));
        }

        return new BigInteger(numeric.toString()).mod(BigInteger.valueOf(97)).intValue() == 1;

    }

    @Override
    public String toString(){
        return value;
    }
}
