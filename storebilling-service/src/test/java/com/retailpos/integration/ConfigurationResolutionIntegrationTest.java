package com.retailpos.integration;

import com.jayway.jsonpath.JsonPath;
import com.retailpos.store.repository.StoreRepository;
import com.retailpos.terminal.repository.TerminalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Hits {@code GET /api/v1/config/{key}} with varying scope params against the real V8-seeded
 * data, asserting the exact resolution chain described in the migration:
 * TERMINAL -&gt; STORE -&gt; ORGANIZATION -&gt; BUSINESS_TYPE -&gt; GLOBAL -&gt; DEFAULT.
 */
@AutoConfigureMockMvc
class ConfigurationResolutionIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private TerminalRepository terminalRepository;

    private String accessToken;
    private Long goldStoreId;
    private Long goldTerminalId;
    private Long groceryStoreId;
    private Long generalStoreId;

    @BeforeEach
    void setUp() throws Exception {
        var loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + TEST_BOOTSTRAP_ADMIN_USERNAME + "\",\"password\":\""
                                + TEST_BOOTSTRAP_ADMIN_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        accessToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.accessToken");

        goldStoreId = storeRepository.findByCode("STORE-GOLD-01").orElseThrow().getId();
        goldTerminalId = terminalRepository.findByStoreIdAndCode(goldStoreId, "TERM-01").orElseThrow().getId();
        groceryStoreId = storeRepository.findByCode("STORE-GRC-01").orElseThrow().getId();
        generalStoreId = storeRepository.findByCode("STORE-GEN-01").orElseThrow().getId();
    }

    @Test
    void defaultCurrencyResolvesGlobalWhenNoStoreGiven() throws Exception {
        mockMvc.perform(get("/api/v1/config/DEFAULT_CURRENCY").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.value").value("USD"))
                .andExpect(jsonPath("$.data.resolvedScopeType").value("GLOBAL"));
    }

    @Test
    void defaultCurrencyResolvesStoreOverrideForGoldStore() throws Exception {
        mockMvc.perform(get("/api/v1/config/DEFAULT_CURRENCY")
                        .param("storeId", String.valueOf(goldStoreId))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.value").value("INR"))
                .andExpect(jsonPath("$.data.resolvedScopeType").value("STORE"));
    }

    @Test
    void showGoldRateResolvesBusinessTypeDefaultForGoldStoreWithoutTerminal() throws Exception {
        mockMvc.perform(get("/api/v1/config/SHOW_GOLD_RATE")
                        .param("storeId", String.valueOf(goldStoreId))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.value").value("true"))
                .andExpect(jsonPath("$.data.resolvedScopeType").value("BUSINESS_TYPE"));
    }

    @Test
    void showGoldRateResolvesTerminalOverrideForGoldTerminal01() throws Exception {
        mockMvc.perform(get("/api/v1/config/SHOW_GOLD_RATE")
                        .param("terminalId", String.valueOf(goldTerminalId))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.value").value("false"))
                .andExpect(jsonPath("$.data.resolvedScopeType").value("TERMINAL"));
    }

    @Test
    void showBatchResolvesBusinessTypeDefaultForGroceryStore() throws Exception {
        mockMvc.perform(get("/api/v1/config/SHOW_BATCH")
                        .param("storeId", String.valueOf(groceryStoreId))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.value").value("true"))
                .andExpect(jsonPath("$.data.resolvedScopeType").value("BUSINESS_TYPE"));
    }

    @Test
    void showGoldRateIsFalseForGeneralStore() throws Exception {
        mockMvc.perform(get("/api/v1/config/SHOW_GOLD_RATE")
                        .param("storeId", String.valueOf(generalStoreId))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.value").value("false"))
                .andExpect(jsonPath("$.data.resolvedScopeType").value("BUSINESS_TYPE"));
    }
}
