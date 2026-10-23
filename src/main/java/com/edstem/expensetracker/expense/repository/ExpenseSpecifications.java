package com.edstem.expensetracker.expense.repository;

import com.edstem.expensetracker.expense.entity.Category;
import com.edstem.expensetracker.expense.entity.Expense;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpenseSpecifications {

    private static final String EXPENSE_DATE = "expenseDate";
    private static final String CATEGORY = "category";

    public static Specification<Expense> matching(LocalDate from, LocalDate to, Category category) {
        List<Specification<Expense>> criteria = new ArrayList<>();
        if (from != null) {
            criteria.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get(EXPENSE_DATE), from));
        }
        if (to != null) {
            criteria.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get(EXPENSE_DATE), to));
        }
        if (category != null) {
            criteria.add((root, query, cb) -> cb.equal(root.get(CATEGORY), category));
        }
        return Specification.allOf(criteria);
    }
}
