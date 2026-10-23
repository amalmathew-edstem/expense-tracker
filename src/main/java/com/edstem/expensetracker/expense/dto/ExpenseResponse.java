package com.edstem.expensetracker.expense.dto;

import com.edstem.expensetracker.expense.entity.Category;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(Long id, BigDecimal amount, Category category, LocalDate date, String note) {}
