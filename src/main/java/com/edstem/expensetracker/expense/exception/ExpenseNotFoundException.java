package com.edstem.expensetracker.expense.exception;

import com.edstem.expensetracker.common.exception.NotFoundException;

public class ExpenseNotFoundException extends NotFoundException {

    public ExpenseNotFoundException(Long id) {
        super("Expense " + id + " was not found");
    }
}
