package com.edstem.expensetracker.expense.mapper;

import com.edstem.expensetracker.expense.dto.CategoryTotalResponse;
import com.edstem.expensetracker.expense.dto.ExpenseRequest;
import com.edstem.expensetracker.expense.dto.ExpenseResponse;
import com.edstem.expensetracker.expense.dto.MonthlySummaryResponse;
import com.edstem.expensetracker.expense.entity.Category;
import com.edstem.expensetracker.expense.entity.Expense;
import com.edstem.expensetracker.expense.repository.CategoryTotal;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    private static final int MONEY_SCALE = 2;
    private static final BigDecimal NO_SPENDING = BigDecimal.ZERO.setScale(MONEY_SCALE);

    public Expense toEntity(ExpenseRequest request) {
        return new Expense(request.amount(), request.category(), request.date(), request.note());
    }

    public ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getExpenseDate(),
                expense.getNote());
    }

    public MonthlySummaryResponse toSummary(YearMonth month, List<CategoryTotal> totals) {
        Map<Category, BigDecimal> totalsByCategory = new EnumMap<>(Category.class);
        totals.forEach(total -> totalsByCategory.put(total.category(), total.total()));

        List<CategoryTotalResponse> categories = Arrays.stream(Category.values())
                .map(category ->
                        new CategoryTotalResponse(category, totalsByCategory.getOrDefault(category, NO_SPENDING)))
                .toList();
        BigDecimal overall =
                categories.stream().map(CategoryTotalResponse::total).reduce(NO_SPENDING, BigDecimal::add);
        return new MonthlySummaryResponse(month, categories, overall);
    }
}
