package com.cbbank.accountservice;

import com.cbbank.accountservice.repo.DummyRepo;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DBRider
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
class DummyMvcTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    DummyRepo repo;

    @Test
    @SneakyThrows
    @DisplayName("Should have working MVC setup")
    void testMVC() {
        // Access via MockMVC
        mockMvc.perform(get("/dummy"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        // Direct Access via Injection
        assertThat(repo.findAll()).isEmpty();
    }

    @Test
    @SneakyThrows
    @DataSet("dummy-data.yml")
    @ExpectedDataSet("dummy-data.yml")
    @DisplayName("Should have working DBRider setup")
    void testDBRider() {
        assertThat(repo.findAll()).isNotEmpty();
    }
}
