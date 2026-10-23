package com.edstem.expensetracker.expense.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edstem.expensetracker.TestcontainersConfiguration;
import com.edstem.expensetracker.expense.repository.ExpenseRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.math.BigDecimal;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExpenseRepository expenseRepository;

    @BeforeEach
    void clearExpenses() {
        expenseRepository.deleteAll();
    }

    @Test
    void createsReadsUpdatesAndDeletesAnExpense() throws Exception {
        String location = createExpense("12.50", "FOOD", "2026-03-14", "Lunch")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(12.50))
                .andExpect(jsonPath("$.category").value("FOOD"))
                .andExpect(jsonPath("$.date").value("2026-03-14"))
                .andExpect(jsonPath("$.note").value("Lunch"))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("Lunch"));

        mockMvc.perform(put(location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseJson("40.00", "TRAVEL", "2026-03-15", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(40.00))
                .andExpect(jsonPath("$.category").value("TRAVEL"))
                .andExpect(jsonPath("$.date").value("2026-03-15"))
                .andExpect(jsonPath("$.note").doesNotExist());

        mockMvc.perform(delete(location)).andExpect(status().isNoContent());
        mockMvc.perform(get(location)).andExpect(status().isNotFound());
    }

    @Test
    void createReturnsTheNewExpenseLocation() throws Exception {
        createExpense("5.00", "OTHER", "2026-03-01", null)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/expenses/\\d+$")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-1.00", "10.001"})
    void rejectsAnAmountThatIsNotPositiveWithAtMostTwoDecimals(String amount) throws Exception {
        createExpense(amount, "FOOD", "2026-03-14", null).andExpect(status().isBadRequest());
    }

    @Test
    void rejectsAnUnknownCategory() throws Exception {
        createExpense("10.00", "GROCERIES", "2026-03-14", null).andExpect(status().isBadRequest());
    }

    @Test
    void rejectsAnExpenseWithoutADate() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 10.00, \"category\": \"FOOD\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returnsNotFoundForAnUnknownExpense() throws Exception {
        mockMvc.perform(get("/api/expenses/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Expense 999999 was not found"));
        mockMvc.perform(put("/api/expenses/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseJson("1.00", "FOOD", "2026-03-14", null)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/expenses/999999")).andExpect(status().isNotFound());
    }

    @Test
    void filtersTheListByInclusiveDateRangeAndCategory() throws Exception {
        createExpense("1.00", "FOOD", "2026-02-28", "before range");
        createExpense("2.00", "FOOD", "2026-03-01", "range start");
        createExpense("3.00", "TRAVEL", "2026-03-10", "other category");
        createExpense("4.00", "FOOD", "2026-03-31", "range end");
        createExpense("5.00", "FOOD", "2026-04-01", "after range");

        mockMvc.perform(get("/api/expenses").param("from", "2026-03-01").param("to", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].note")
                        .value(containsInAnyOrder("range start", "other category", "range end")));

        mockMvc.perform(get("/api/expenses")
                        .param("from", "2026-03-01")
                        .param("to", "2026-03-31")
                        .param("category", "FOOD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].note").value(containsInAnyOrder("range start", "range end")))
                .andExpect(jsonPath("$.page.totalElements").value(2));

        mockMvc.perform(get("/api/expenses").param("category", "TRAVEL"))
                .andExpect(jsonPath("$.content[*].note").value(containsInAnyOrder("other category")));
    }

    @Test
    void rejectsADateRangeThatEndsBeforeItStarts() throws Exception {
        mockMvc.perform(get("/api/expenses").param("from", "2026-03-31").param("to", "2026-03-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void monthlySummaryCountsTheFirstAndLastDayAndExcludesNeighbouringMonths() throws Exception {
        createExpense("100.00", "BILLS", "2026-02-28", null);
        createExpense("10.00", "FOOD", "2026-03-01", null);
        createExpense("20.00", "TRAVEL", "2026-03-31", null);
        createExpense("7.25", "FOOD", "2026-03-15", null);
        createExpense("100.00", "BILLS", "2026-04-01", null);

        mockMvc.perform(get("/api/expenses/summary").param("month", "2026-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("2026-03"))
                .andExpect(
                        jsonPath("$.categories[?(@.category == 'FOOD')].total").value(17.25))
                .andExpect(jsonPath("$.categories[?(@.category == 'TRAVEL')].total")
                        .value(20.00))
                .andExpect(
                        jsonPath("$.categories[?(@.category == 'BILLS')].total").value(0.00))
                .andExpect(
                        jsonPath("$.categories[?(@.category == 'OTHER')].total").value(0.00))
                .andExpect(jsonPath("$.total").value(37.25));
    }

    @Test
    void monthlySummaryTotalsAreExactToTheCent() throws Exception {
        createExpense("0.10", "OTHER", "2026-03-05", null);
        createExpense("0.20", "OTHER", "2026-03-06", null);

        String body = mockMvc.perform(get("/api/expenses/summary").param("month", "2026-03"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode summary = objectMapper
                .reader()
                .with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .with(JsonNodeFactory.withExactBigDecimals(true))
                .readTree(body);
        BigDecimal otherTotal = StreamSupport.stream(summary.get("categories").spliterator(), false)
                .filter(category -> "OTHER".equals(category.get("category").asText()))
                .map(category -> category.get("total").decimalValue())
                .findFirst()
                .orElseThrow();
        assertThat(summary.get("total").decimalValue()).isEqualTo(new BigDecimal("0.30"));
        assertThat(otherTotal).isEqualTo(new BigDecimal("0.30"));
    }

    @Test
    void monthlySummaryHandlesTheLastDayOfAShortMonth() throws Exception {
        createExpense("3.00", "FOOD", "2027-02-28", null);
        createExpense("4.00", "FOOD", "2027-03-01", null);

        mockMvc.perform(get("/api/expenses/summary").param("month", "2027-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3.00));
    }

    @Test
    void rejectsAMalformedSummaryMonth() throws Exception {
        mockMvc.perform(get("/api/expenses/summary").param("month", "March")).andExpect(status().isBadRequest());
    }

    private ResultActions createExpense(String amount, String category, String date, String note) throws Exception {
        return mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(expenseJson(amount, category, date, note)));
    }

    private static String expenseJson(String amount, String category, String date, String note) {
        String noteJson = note == null ? "null" : "\"" + note + "\"";
        return """
                {"amount": %s, "category": "%s", "date": "%s", "note": %s}
                """.formatted(amount, category, date, noteJson);
    }
}
