package com.cbbank.accountservice.advice;

import com.cbbank.accountservice.exceptions.AccountOverchargeException;
import com.cbbank.accountservice.exceptions.EntityNotFoundException;
import com.cbbank.accountservice.exceptions.InvalidIbanException;
import com.cbbank.accountservice.exceptions.NonceInvalidException;
import com.cbbank.accountservice.exceptions.NoopTransactionException;
import com.cbbank.accountservice.exceptions.UnrelatedAccountException;
import com.cbbank.accountservice.exceptions.UnsupportedCurrencyException;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.StatusType;

import java.net.URI;

@ControllerAdvice
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProblemAdvice {
    @ExceptionHandler(NonceInvalidException.class)
    public ResponseEntity<Problem> nonceInvalid(NonceInvalidException exception) {
        return makeProblem("nonce-invalid", Status.CONFLICT, "Nonce is invalid");
    }

    @ExceptionHandler(AccountOverchargeException.class)
    public ResponseEntity<Problem> accountOvercharge(AccountOverchargeException exception) {
        return makeProblem("account-overcharge", Status.CONFLICT, "Attempted to overcharge account");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Problem> entityNotFound(EntityNotFoundException exception) {
        return makeProblem("entity-not-fount", Status.NOT_FOUND, "Entity not found");
    }

    @ExceptionHandler(InvalidIbanException.class)
    public ResponseEntity<Problem> invalidIban(InvalidIbanException exception) {
        return makeProblem("invalid-iban", Status.BAD_REQUEST, "IBAN not valid");
    }

    @ExceptionHandler(NoopTransactionException.class)
    public ResponseEntity<Problem> noopTransaction(NoopTransactionException exception) {
        return makeProblem("noop-transaction", Status.BAD_REQUEST, "Identical source and target IBAN");
    }

    @ExceptionHandler(UnrelatedAccountException.class)
    public ResponseEntity<Problem> unrelatedAccount(UnrelatedAccountException exception) {
        return makeProblem("unrelated-account", Status.BAD_REQUEST,
                "Neither source nor target IBAN are related to the supplied account id");
    }

    @ExceptionHandler(UnsupportedCurrencyException.class)
    public ResponseEntity<Problem> unsupportedCurrency(UnsupportedCurrencyException exception) {
        return makeProblem("unsupported-currency", Status.BAD_REQUEST,
                "Transaction uses a currency different to the account");
    }

    @SneakyThrows
    private ResponseEntity<Problem> makeProblem(
            String title,
            StatusType status,
            String message) {
        return ResponseEntity.status(status.getStatusCode())
                .contentType(MediaType.valueOf("application/problem+json"))
                .body(Problem.builder()
                        .withType(new URI("urn::com.cbbank.accountservice." + title))
                        .withDetail(message)
                        .withTitle(title)
                        .withStatus(status)
                        .build());
    }
}
