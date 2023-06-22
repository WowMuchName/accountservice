package com.cbbank.accountservice.domain;

import com.cbbank.accountservice.entity.Account;
import com.cbbank.accountservice.entity.FinancialTransaction;
import com.cbbank.accountservice.entity.NonceToken;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Set;

@Value
@Jacksonized
@RequiredArgsConstructor
@Builder(toBuilder = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AccountRepresentation {
    public static final String ACCOUNT_MIME = "application/vnd.com.cbbank.accountservice.account-v1+json";
    public static final String ACCOUNT_LIST_MIME = "application/vnd.com.cbbank.accountservice.account-list-v1+json";

    String id;
    String iban;
    String firstName;
    String lastName;
    // Note: All amounts are understood in cents / Smallest unit possible for that currency
    Long balance;
    Currency currency;
    Long overcharge;
    Long creditLimit;

    public static class AccountRepresentationBuilder {
        public AccountRepresentationBuilder from(Account account) {
            return id(account.getId())
                    .iban(account.getIban())
                    .firstName(account.getFirst_name())
                    .lastName(account.getLast_name())
                    .balance(account.getBalance())
                    .currency(account.getCurrency())
                    .overcharge(account.getOvercharge())
                    .creditLimit(account.getBalance() + account.getOvercharge());
        }
    }
}
