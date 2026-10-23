package com.edstem.expensetracker.expense.dto;

import com.edstem.expensetracker.expense.entity.Category;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotNull @Positive @Digits(integer = 10, fraction = 2)
        BigDecimal amount,

        @NotNull Category category,
        @NotNull LocalDate date,
        @Size(max = 500) String note) {}
