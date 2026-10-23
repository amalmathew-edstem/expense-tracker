package com.edstem.expensetracker.expense.service;

import com.edstem.expensetracker.expense.dto.ExpenseFilter;
import com.edstem.expensetracker.expense.dto.ExpenseRequest;
import com.edstem.expensetracker.expense.dto.ExpenseResponse;
import com.edstem.expensetracker.expense.dto.MonthlySummaryResponse;
import com.edstem.expensetracker.expense.entity.Expense;
import com.edstem.expensetracker.expense.exception.ExpenseNotFoundException;
import com.edstem.expensetracker.expense.mapper.ExpenseMapper;
import com.edstem.expensetracker.expense.repository.ExpenseRepository;
import com.edstem.expensetracker.expense.repository.ExpenseSpecifications;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;

    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        Expense saved = expenseRepository.save(expenseMapper.toEntity(request));
        return expenseMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long id) {
        return expenseMapper.toResponse(findExpense(id));
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponse> list(ExpenseFilter filter, Pageable pageable) {
        return expenseRepository
                .findAll(ExpenseSpecifications.matching(filter.from(), filter.to(), filter.category()), pageable)
                .map(expenseMapper::toResponse);
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseRequest request) {
        Expense expense = findExpense(id);
        expense.revise(request.amount(), request.category(), request.date(), request.note());
        return expenseMapper.toResponse(expenseRepository.saveAndFlush(expense));
    }

    @Transactional
    public void delete(Long id) {
        expenseRepository.delete(findExpense(id));
    }

    @Transactional(readOnly = true)
    public MonthlySummaryResponse summarise(YearMonth month) {
        return expenseMapper.toSummary(
                month, expenseRepository.sumByCategoryBetween(month.atDay(1), month.atEndOfMonth()));
    }

    private Expense findExpense(Long id) {
        return expenseRepository.findById(id).orElseThrow(() -> new ExpenseNotFoundException(id));
    }
}
