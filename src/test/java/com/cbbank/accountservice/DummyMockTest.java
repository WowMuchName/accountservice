package com.cbbank.accountservice;

import com.cbbank.accountservice.entity.DummyEntity;
import com.cbbank.accountservice.repo.DummyRepo;
import com.cbbank.accountservice.service.DummyService;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;
import io.restassured.RestAssured;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DummyMockTest {
    @MockBean
    DummyRepo repo;

    @Autowired
    DummyService service;

    @Test
    @DisplayName("Should have working Integration-Test setup")
    void testMock() {
        when(repo.findAll())
                .thenReturn(List.of(new DummyEntity("1", "test")));

        assertThat(service.list()).hasSize(1);

        verify(repo).findAll();
        verifyNoMoreInteractions(repo);
    }
}
