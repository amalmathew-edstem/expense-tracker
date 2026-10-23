package com.edstem.expensetracker.expense.repository;

import com.edstem.expensetracker.expense.entity.Category;
import java.math.BigDecimal;

public record CategoryTotal(Category category, BigDecimal total) {}
