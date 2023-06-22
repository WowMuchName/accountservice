package com.cbbank.accountservice.domain;

import com.cbbank.accountservice.entity.FinancialTransaction;
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
public class TransactionRepresentation {
    public static final String TRANSACTION_MIME = "application/vnd.com.cbbank.accountservice.transaction-v1+json";
    public static final String TRANSACTION_LIST_MIME = "application/vnd.com.cbbank.accountservice.transaction-list-v1+json";

    String id;
    String sourceIban;
    String targetIban;
    Currency currency;
    Long amount;
    Long time;

    public static class TransactionRepresentationBuilder {
        public TransactionRepresentationBuilder from(FinancialTransaction transaction) {
            return id(transaction.getId())
                    .sourceIban(transaction.getSource_iban())
                    .targetIban(transaction.getTarget_iban())
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .time(transaction.getTime());
        }
    }
}
