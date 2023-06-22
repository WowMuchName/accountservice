package com.cbbank.accountservice.entity;

import com.cbbank.accountservice.domain.Currency;
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
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import java.util.SortedSet;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @NonNull
    String iban;
    @NonNull
    String first_name;
    @NonNull
    String last_name;
    @NonNull
    Long balance;
    @NonNull
    Currency currency;
    @NonNull
    Long overcharge;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "account")
    SortedSet<FinancialTransaction> financialTransactions;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "account")
    SortedSet<NonceToken> nonceTokens;
}
