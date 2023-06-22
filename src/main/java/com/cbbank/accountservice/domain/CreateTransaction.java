package com.cbbank.accountservice.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@RequiredArgsConstructor
@Builder(toBuilder = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CreateTransaction {
    public static final String CREATE_TRANSACTION_MIME = "application/vnd.com.cbbank.accountservice.create_transaction-v1+json";

    String sourceIban;
    String targetIban;
    Currency currency;
    Long amount;
    String nonce;
}
