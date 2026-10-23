package com.edstem.expensetracker.expense.dto;

import com.edstem.expensetracker.expense.entity.Category;
import java.math.BigDecimal;

public record CategoryTotalResponse(Category category, BigDecimal total) {}
