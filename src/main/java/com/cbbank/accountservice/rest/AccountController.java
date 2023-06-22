package com.cbbank.accountservice.rest;

import com.cbbank.accountservice.domain.AccountRepresentation;
import com.cbbank.accountservice.domain.CreateTransaction;
import com.cbbank.accountservice.domain.NonceRepresentation;
import com.cbbank.accountservice.domain.TransactionRepresentation;
import com.cbbank.accountservice.entity.FinancialTransaction;
import com.cbbank.accountservice.exceptions.EntityNotFoundException;
import com.cbbank.accountservice.service.AccountService;
import com.cbbank.accountservice.service.FinancialTransactionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.SortedSet;

@RestController
@RequestMapping("account")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AccountController {
    AccountService accountService;
    FinancialTransactionService transactionService;

    @GetMapping(value = "{id}", produces = AccountRepresentation.ACCOUNT_MIME)
    public AccountRepresentation get(@PathVariable("id") String id) {
        return AccountRepresentation.builder()
                .from(accountService.getAccount(id).orElseThrow(EntityNotFoundException::new)).build();
    }

    private TransactionRepresentation mapTransaction(FinancialTransaction transaction) {
        return TransactionRepresentation.builder().from(transaction).build();
    }

    @GetMapping(value = "{id}/transaction", produces = TransactionRepresentation.TRANSACTION_LIST_MIME)
    public List<TransactionRepresentation> getTransactions(@PathVariable("id") String id) {
        SortedSet<FinancialTransaction> list = accountService.getAccount(id).orElseThrow(EntityNotFoundException::new)
                .getFinancialTransactions();
        return list.stream().map(this::mapTransaction)
                .sorted((TransactionRepresentation a, TransactionRepresentation b) ->
                        Math.toIntExact(a.getTime() - b.getTime())).toList();
    }

    @PostMapping(value = "{id}/nonce", produces = NonceRepresentation.NONCE_MIME)
    public NonceRepresentation getNonce(@PathVariable("id") String id) {
        return NonceRepresentation.builder().from(transactionService.getNonce(
                accountService.getAccount(id).orElseThrow(EntityNotFoundException::new))).build();
    }

    @PutMapping(
            value = "{id}/transaction",
            produces = TransactionRepresentation.TRANSACTION_MIME,
            consumes = CreateTransaction.CREATE_TRANSACTION_MIME
    )
    public TransactionRepresentation putTransaction(@PathVariable("id") String id,
                                                    @Valid @RequestBody CreateTransaction createTransaction) {
        return TransactionRepresentation.builder().from(transactionService.makeTransaction(
                id,
                createTransaction)).build();
    }
}
