package com.cbbank.accountservice;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.hamcrest.Matchers.equalTo;

@DBRider
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DummyIT {
    @LocalServerPort
    Integer serverPort;

    @BeforeAll
    void setup() {
        RestAssured.baseURI = "http://localhost:" + serverPort;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @DataSet("dummy-data.yml")
    @DisplayName("Should have working Integration-Test setup")
    void testEE() {
        RestAssured.get("/dummy")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].id", equalTo("1"))
                .body("[0].v", equalTo("test"));
    }
}
