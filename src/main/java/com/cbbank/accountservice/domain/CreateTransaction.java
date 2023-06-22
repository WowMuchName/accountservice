package com.cbbank.accountservice.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import org.springframework.validation.annotation.Validated;

@Value
@Validated
@Jacksonized
@RequiredArgsConstructor
@Builder(toBuilder = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CreateTransaction {
    public static final String CREATE_TRANSACTION_MIME = "application/vnd.com.cbbank.accountservice.create_transaction-v1+json";

    @NotBlank
    String sourceIban;
    @NotBlank
    String targetIban;
    @NotNull
    Currency currency;
    @Positive
    Long amount;
    @NotBlank
    String nonce;
}
