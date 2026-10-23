package com.edstem.expensetracker.expense.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record MonthlySummaryResponse(YearMonth month, List<CategoryTotalResponse> categories, BigDecimal total) {}
