package com.cbbank.accountservice.service;

import com.cbbank.accountservice.domain.CreateTransaction;
import com.cbbank.accountservice.entity.Account;
import com.cbbank.accountservice.entity.FinancialTransaction;
import com.cbbank.accountservice.entity.NonceToken;
import com.cbbank.accountservice.exceptions.AccountOverchargeException;
import com.cbbank.accountservice.exceptions.EntityNotFoundException;
import com.cbbank.accountservice.exceptions.NonceInvalidException;
import com.cbbank.accountservice.exceptions.NoopTransactionException;
import com.cbbank.accountservice.exceptions.UnrelatedAccountException;
import com.cbbank.accountservice.exceptions.UnsupportedCurrencyException;
import com.cbbank.accountservice.repo.AccountRepo;
import com.cbbank.accountservice.repo.FinancialTransactionRepo;
import com.cbbank.accountservice.repo.NonceTokenRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class FinancialTransactionService { // TODO Stuffing everything into one service is not optimal
    FinancialTransactionRepo transactionRepo;
    AccountRepo accountRepo;
    NonceTokenRepo nonceRepo;

    public NonceToken getNonce(Account account) {
        var nonce = NonceToken.builder().account(account).build(); // Sets default timestamp to now
        return nonceRepo.save(nonce);
    }

    private boolean validateAndDecideIfIncoming(Account account, CreateTransaction createTransaction) {
        if (createTransaction.getSourceIban().equals(account.getIban())) {
            if (createTransaction.getTargetIban().equals(account.getIban())) {
                throw new NoopTransactionException();
            }
            var amountLimit = account.getBalance() + account.getOvercharge();
            if (amountLimit < createTransaction.getAmount()) {
                throw new AccountOverchargeException();
            }
            return false;
        } else if(!createTransaction.getTargetIban().equals(account.getIban())) {
            throw new UnrelatedAccountException();
        }
        return true;
    }

    public FinancialTransaction makeTransaction(String accountId, CreateTransaction createTransaction) {
        // TODO Transaction

        var account = accountRepo.findById(accountId).orElseThrow(EntityNotFoundException::new);
        var incoming = validateAndDecideIfIncoming(account, createTransaction);

        validateCurrency(createTransaction, account);

        var nonce = account.getNonceTokens().stream().filter(n -> n.getId().equals(createTransaction.getNonce()))
                .findFirst().orElseThrow(NonceInvalidException::new);
        nonceRepo.delete(nonce);

        if (incoming) {
            account.setBalance(account.getBalance() + createTransaction.getAmount());
        } else {
            account.setBalance(account.getBalance() - createTransaction.getAmount());
        }
        return transactionRepo.save(FinancialTransaction.builder()
                        .from(createTransaction)
                        .account(account)
                        .build());
    }

    private static void validateCurrency(CreateTransaction createTransaction, Account account) {
        if (!account.getCurrency().equals(createTransaction.getCurrency())) {
            throw new UnsupportedCurrencyException();
        }
    }
}
