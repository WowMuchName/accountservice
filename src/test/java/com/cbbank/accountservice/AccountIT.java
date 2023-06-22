package com.cbbank.accountservice;

import com.cbbank.accountservice.domain.AccountRepresentation;
import com.cbbank.accountservice.domain.CreateTransaction;
import com.cbbank.accountservice.domain.Currency;
import com.cbbank.accountservice.domain.NonceRepresentation;
import com.cbbank.accountservice.domain.TransactionRepresentation;
import com.cbbank.accountservice.entity.Account;
import com.cbbank.accountservice.repo.AccountRepo;
import com.cbbank.accountservice.repo.FinancialTransactionRepo;
import com.cbbank.accountservice.repo.NonceTokenRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.junit5.api.DBRider;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.zalando.problem.Problem;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DBRider
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountIT {
    private static final String TEST_IBAN_1 = "DE02120300000000202051";
    private static final String TEST_IBAN_2 = "DE02500105170137075030";
    private static final long STARTING_BALANCE = 5000L;
    private static final long STARTING_OVERCHARGE = 5000L;

    @LocalServerPort
    Integer serverPort;

    @Autowired
    FinancialTransactionRepo financialTransactionRepo;

    @Autowired
    AccountRepo accountRepo;

    @Autowired
    NonceTokenRepo nonceRepo;

    @Autowired
    ObjectMapper mapper;

    @BeforeAll
    void setup() {
        RestAssured.baseURI = "http://localhost:" + serverPort;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    void restDB() {
        financialTransactionRepo.deleteAll();
        nonceRepo.deleteAll();
        var account = accountRepo.findById("1").get();
        account.setBalance(STARTING_BALANCE);
        accountRepo.save(account);
    }

    @Test
    @DisplayName("Should initialize account with setup script")
    void testInitialization() {
        RestAssured.get("/account/1")
                .then()
                .statusCode(200)
                .contentType(AccountRepresentation.ACCOUNT_MIME)
                .body("iban", equalTo( "DE02120300000000202051"))
                .body("overcharge", equalTo(5000))
                .body("balance", equalTo(5000)) // Note "downgraded" to int by restassured
                .body("currency", equalTo("EURO")) // DB encodes by ordinal, jackson by name
                .body("firstName", equalTo("Jon"))
                .body("lastName", equalTo("Doe"))
                .body("creditLimit", equalTo(10000))
        ;
    }

    @Test
    @DisplayName("Should be able to obtain nonce")
    void testNonce() {
        var nonce = getNonce();
        assertThat(nonce.getId()).isNotBlank();
        assertThat(nonce.getTime()).isLessThanOrEqualTo(System.currentTimeMillis());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should be able to obtain a list of transactions")
    void testTransactionList() {
        var transaction1 = makeAndVerifyAValidTransaction();
        var transaction2 = makeAndVerifyAValidTransaction();
        var genericList = getTransactionList();
        assertThat(genericList.get(0)).isEqualTo(transaction1);
        assertThat(genericList.get(1)).isEqualTo(transaction2);
    }

    @SneakyThrows
    private List<TransactionRepresentation> getTransactionList() {
        return mapper.readValue(RestAssured.get("/account/1/transaction")
                .then()
                .statusCode(200)
                .contentType(TransactionRepresentation.TRANSACTION_LIST_MIME)
                .extract().body().asString(), new TypeReference<List<TransactionRepresentation>>() {
        });
    }

    @Test
    @SneakyThrows
    @DisplayName("Should not be able to overcharge account")
    void testOvercharge() {
        makeTransaction(STARTING_BALANCE * 3, TEST_IBAN_1, TEST_IBAN_2).statusCode(409);
    }

    @Test
    @SneakyThrows
    @DisplayName("Should be able to receive transaction")
    void testReceive() {
        var transaction = makeAndVerifyAValidTransaction(STARTING_BALANCE * 3, TEST_IBAN_2, TEST_IBAN_1);

        var account = getAccountRepresentation();
        assertThat(account.getBalance()).isEqualTo(STARTING_BALANCE * 4);
        assertThat(account.getCreditLimit()).isEqualTo(STARTING_BALANCE * 4 + STARTING_OVERCHARGE);

        var transactions = getTransactionList();
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0)).isEqualTo(transaction);
    }


    @Test
    @SneakyThrows
    @DisplayName("Should not be to use the same nonce twice")
    void testDupeNonce() {
        var nonce = getNonce();
        CreateTransaction createTransaction = CreateTransaction.builder()
                .amount(100L)
                .sourceIban(TEST_IBAN_1)
                .targetIban(TEST_IBAN_2)
                .currency(Currency.EURO)
                .nonce(nonce.getId())
                .build();

        // Succeed once
        RestAssured.given()
                .contentType(CreateTransaction.CREATE_TRANSACTION_MIME)
                .body(createTransaction)
                .put("/account/1/transaction")
                .then()
                .statusCode(200);

        // Fail the second time
        var problem = RestAssured.given()
                .contentType(CreateTransaction.CREATE_TRANSACTION_MIME)
                .body(createTransaction)
                .put("/account/1/transaction")
                .then()
                .statusCode(409)
                .extract().body().as(Problem.class);

        // Verify that nothing was written twice
        Account account = getAccount();
        assertThat(account.getFinancialTransactions()).describedAs("Dupe transaction should not be persisted")
                .hasSize(1);
        assertThat(account.getBalance()).describedAs("Dupe transaction should not apply balance")
                .isEqualTo(STARTING_BALANCE - 100L);
    }

    // TODO It would be cleaner to use getAccountRepresentation unless not possible
    private Account getAccount() {
        return accountRepo.findById("1").get();
    }

    private AccountRepresentation getAccountRepresentation() {
        return RestAssured.get("/account/1")
                .then()
                .statusCode(200)
                .contentType(AccountRepresentation.ACCOUNT_MIME)
                .extract().body().as(AccountRepresentation.class);
    }

    @Test
    @DisplayName("Should be able to make transaction")
    void testTransaction() {
        makeAndVerifyAValidTransaction(1000L, TEST_IBAN_1, TEST_IBAN_2);
        var account = getAccountRepresentation();
        assertThat(account.getCreditLimit()).isEqualTo(STARTING_BALANCE + STARTING_OVERCHARGE - 1000L);
        assertThat(account.getBalance()).isEqualTo(STARTING_BALANCE - 1000L);
    }

    @Test
    @DisplayName("Should not be able to use different currency than the account uses")
    void testForeignCurrency() {
        var nonce = getNonce();

        RestAssured.given()
                .contentType(CreateTransaction.CREATE_TRANSACTION_MIME)
                .body(CreateTransaction.builder()
                        .amount(10L)
                        .sourceIban(TEST_IBAN_1)
                        .targetIban(TEST_IBAN_2)
                        .currency(Currency.DOLLAR)
                        .nonce(nonce.getId())
                        .build()
                     )
                .put("/account/1/transaction")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should be able to overcharge within limit")
    void testOverchargeWithinLimit() {
        makeAndVerifyAValidTransaction(STARTING_BALANCE + STARTING_OVERCHARGE, TEST_IBAN_1, TEST_IBAN_2);
        var account = getAccountRepresentation();
        assertThat(account.getBalance()).isEqualTo(-STARTING_OVERCHARGE);
        assertThat(account.getCreditLimit()).isZero();
    }

    @Test
    @DisplayName("Should not be able to submit invalid transactions")
    void testInvalidTransaction() {
        var problem = makeTransaction(1L, " ", TEST_IBAN_2).statusCode(400).extract().body()
                .as(Problem.class);
        assertThat(problem.getType().toASCIIString())
                .isEqualTo("https://zalando.github.io/problem/constraint-violation");
        problem = makeTransaction(1L, TEST_IBAN_1, " ").statusCode(400).extract().body()
                .as(Problem.class);
        assertThat(problem.getType().toASCIIString())
                .isEqualTo("https://zalando.github.io/problem/constraint-violation");
        problem = makeTransaction(-1L, TEST_IBAN_1, TEST_IBAN_2).statusCode(400).extract().body()
                .as(Problem.class);
        assertThat(problem.getType().toASCIIString())
                .isEqualTo("https://zalando.github.io/problem/constraint-violation");
        verifyInvalidTransactionsHadNoEffect();
    }

    private void verifyInvalidTransactionsHadNoEffect() {
        assertThat(getTransactionList()).describedAs("should not save invalid transactions").isEmpty();
        assertThat(getAccountRepresentation().getBalance())
                .describedAs("should not apply balance with invalid transactions")
                .isEqualTo(STARTING_BALANCE);
    }

    @Test
    @DisplayName("Should not be able to user invalid ibans")
    void testInvalidIbans() {
        var problem = makeTransaction(1L, "DE123", TEST_IBAN_2).statusCode(400).extract().body()
                .as(Problem.class);
        assertThat(problem.getType().toASCIIString()).isEqualTo("urn::com.cbbank.accountservice.invalid-iban");
        makeTransaction(1L, TEST_IBAN_1, "DE123").statusCode(400);
        verifyInvalidTransactionsHadNoEffect();
    }


    private static TransactionRepresentation makeAndVerifyAValidTransaction() {
        return makeAndVerifyAValidTransaction(1000L, TEST_IBAN_1, TEST_IBAN_2);
    }

    @NotNull
    private static TransactionRepresentation makeAndVerifyAValidTransaction(long amount, String sourceIban, String targetIban) {
        var transaction = makeTransaction(amount, sourceIban, targetIban)
                .statusCode(200)
                .contentType(TransactionRepresentation.TRANSACTION_MIME)
                .body("sourceIban", equalTo(sourceIban))
                .body("targetIban", equalTo(targetIban))
                .body("currency", equalTo("EURO")) // DB encodes by ordinal, jackson by name
                .body("amount", equalTo(Math.toIntExact(amount))) // Rest Assured makes this an int in the representation
                .extract().body().as(TransactionRepresentation.class);

        assertThat(transaction.getTime()).isLessThanOrEqualTo(System.currentTimeMillis());
        assertThat(transaction.getId()).isNotBlank();
        return transaction;
    }

    private static ValidatableResponse makeTransaction(long amount, String sourceIban, String targetIban) {
        var nonce = getNonce();

        return RestAssured.given()
                .contentType(CreateTransaction.CREATE_TRANSACTION_MIME)
                .body(CreateTransaction.builder()
                        .amount(amount)
                        .sourceIban(sourceIban)
                        .targetIban(targetIban)
                        .currency(Currency.EURO)
                        .nonce(nonce.getId())
                        .build())
                .put("/account/1/transaction")
                .then();
    }

    private static NonceRepresentation getNonce() {
        return RestAssured.post("/account/1/nonce")
                .then()
                .statusCode(200)
                .contentType(NonceRepresentation.NONCE_MIME)
                .extract().body().as(NonceRepresentation.class);
    }
}
