package com.edstem.expensetracker.expense.dto;

import com.edstem.expensetracker.expense.entity.Category;
import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public record ExpenseFilter(
        @DateTimeFormat(iso = ISO.DATE) LocalDate from,
        @DateTimeFormat(iso = ISO.DATE) LocalDate to,
        Category category) {

    @AssertTrue(message = "from must be on or before to")
    public boolean isDateRangeOrdered() {
        return from == null || to == null || !from.isAfter(to);
    }
}
