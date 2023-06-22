package com.cbbank.accountservice.entity;

import com.cbbank.accountservice.domain.CreateTransaction;
import com.cbbank.accountservice.domain.Currency;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FinancialTransaction implements Comparable<FinancialTransaction> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @NonNull
    String source_iban;
    @NonNull
    String target_iban;
    @NonNull
    Currency currency;
    @NonNull
    Long amount;
    @Default
    Long time = System.currentTimeMillis();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    Account account;

    public static class FinancialTransactionBuilder {
        public FinancialTransactionBuilder from(CreateTransaction createTransaction) {
            return amount(createTransaction.getAmount())
                    .currency(createTransaction.getCurrency())
                    .source_iban(createTransaction.getSourceIban())
                    .target_iban(createTransaction.getTargetIban());
        }
    }

    @Override
    public int compareTo(@NotNull FinancialTransaction o) {
        return Math.toIntExact(time - o.getTime());
    }
}
