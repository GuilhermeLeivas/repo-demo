package com.example.accounts;

import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
@WithMockUser(roles = "OPERATOR")
class AccountApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired AccountRepository accounts;
    @BeforeEach void clean() { accounts.deleteAll(); }
    UUID create(String document) throws Exception {
        var response = mvc.perform(post("/api/accounts").contentType("application/json")
            .content("{\"holder\":\"Pessoa Demo\",\"document\":\"" + document + "\",\"dailyLimit\":100.00}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.maskedDocument").value("*******" + document.substring(7)))
            .andReturn().getResponse().getContentAsString();
        return UUID.fromString(json.readTree(response).get("id").asText());
    }
    void credit(UUID id, String amount) throws Exception {
        mvc.perform(post("/api/accounts/" + id + "/credits").contentType("application/json")
            .content("{\"amount\":" + amount + "}")).andExpect(status().isOk());
    }
    @Test void crudLifecycle() throws Exception {
        var id = create("00000000001");
        mvc.perform(get("/api/accounts/" + id)).andExpect(status().isOk());
        mvc.perform(get("/api/accounts")).andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1));
        mvc.perform(put("/api/accounts/" + id).contentType("application/json")
            .content("{\"holder\":\"Nome atualizado\",\"dailyLimit\":200.00,\"status\":\"ACTIVE\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.holder").value("Nome atualizado"));
        mvc.perform(delete("/api/accounts/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/accounts/" + id)).andExpect(status().isNotFound());
    }
    @Test void duplicateDocumentRejected() throws Exception {
        create("00000000001");
        mvc.perform(post("/api/accounts").contentType("application/json")
            .content("{\"holder\":\"Outro\",\"document\":\"00000000001\",\"dailyLimit\":100}"))
            .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.code").value("DUPLICATE_DOCUMENT"));
    }
    @Test void overdraftRejectedAndBalancePreserved() throws Exception {
        var id = create("00000000001"); credit(id, "10.00");
        mvc.perform(post("/api/accounts/" + id + "/debits").contentType("application/json").content("{\"amount\":20.00}"))
            .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"));
        assertThat(accounts.findById(id).orElseThrow().balance).isEqualByComparingTo("10.00");
    }
    @Test void cumulativeDailyLimitRejected() throws Exception {
        var id = create("00000000001"); credit(id, "300.00");
        mvc.perform(post("/api/accounts/" + id + "/debits").contentType("application/json").content("{\"amount\":60.00}"))
            .andExpect(status().isOk());
        mvc.perform(post("/api/accounts/" + id + "/debits").contentType("application/json").content("{\"amount\":50.00}"))
            .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.code").value("DAILY_LIMIT"));
        assertThat(accounts.findById(id).orElseThrow().balance).isEqualByComparingTo("240.00");
    }
    @Test void blockedAccountCannotMoveFunds() throws Exception {
        var id = create("00000000001"); var a = accounts.findById(id).orElseThrow();
        a.status = Account.Status.BLOCKED; accounts.saveAndFlush(a);
        mvc.perform(post("/api/accounts/" + id + "/credits").contentType("application/json").content("{\"amount\":10.00}"))
            .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.code").value("BLOCKED_ACCOUNT"));
    }
    @Test void nonZeroBalanceCannotBeDeleted() throws Exception {
        var id = create("00000000001"); credit(id, "1.00");
        mvc.perform(delete("/api/accounts/" + id)).andExpect(status().isUnprocessableEntity());
        assertThat(accounts.existsById(id)).isTrue();
    }
    @Test void invalidAmountsRejected() throws Exception {
        var id = create("00000000001");
        for (String amount : new String[]{"0", "-1", "1.001", "100000.01"})
            mvc.perform(post("/api/accounts/" + id + "/credits").contentType("application/json").content("{\"amount\":" + amount + "}"))
                .andExpect(status().isBadRequest());
    }
    @Test @WithMockUser(roles = "AUDITOR") void auditorCannotWrite() throws Exception {
        mvc.perform(post("/api/accounts").contentType("application/json")
            .content("{\"holder\":\"Demo\",\"document\":\"00000000001\",\"dailyLimit\":100}"))
            .andExpect(status().isForbidden());
    }
    @Test @org.springframework.security.test.context.support.WithAnonymousUser
    void anonymousCannotRead() throws Exception { mvc.perform(get("/api/accounts")).andExpect(status().isUnauthorized()); }
    @Test void staleUpdateRejected() throws Exception {
        var id = create("00000000001"); var first = accounts.findById(id).orElseThrow(); var stale = accounts.findById(id).orElseThrow();
        first.balance = new BigDecimal("10.00"); accounts.saveAndFlush(first);
        stale.balance = new BigDecimal("20.00");
        assertThatThrownBy(() -> accounts.saveAndFlush(stale)).isInstanceOf(org.springframework.dao.OptimisticLockingFailureException.class);
    }
    @Test void previousDaySpendResets() throws Exception {
        var id = create("00000000001"); credit(id, "200.00");
        var a = accounts.findById(id).orElseThrow(); a.dailySpent = new BigDecimal("100.00");
        a.spendingDate = java.time.LocalDate.of(2000, 1, 1); accounts.saveAndFlush(a);
        mvc.perform(post("/api/accounts/" + id + "/debits").contentType("application/json").content("{\"amount\":50.00}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(150.00));
    }
}
