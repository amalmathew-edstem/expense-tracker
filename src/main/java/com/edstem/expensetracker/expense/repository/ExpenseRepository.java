package com.edstem.expensetracker.expense.repository;

import com.edstem.expensetracker.expense.entity.Expense;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    @Query("""
            select new com.edstem.expensetracker.expense.repository.CategoryTotal(e.category, sum(e.amount))
            from Expense e
            where e.expenseDate between :start and :end
            group by e.category
            """)
    List<CategoryTotal> sumByCategoryBetween(LocalDate start, LocalDate end);
}
