package com.edstem.expensetracker.expense.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expense")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(length = 500)
    private String note;

    @Version
    private Long version;

    public Expense(BigDecimal amount, Category category, LocalDate expenseDate, String note) {
        this.amount = amount;
        this.category = category;
        this.expenseDate = expenseDate;
        this.note = note;
    }

    public void revise(BigDecimal amount, Category category, LocalDate expenseDate, String note) {
        this.amount = amount;
        this.category = category;
        this.expenseDate = expenseDate;
        this.note = note;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Expense expense)) {
            return false;
        }
        return id != null && id.equals(expense.id);
    }

    @Override
    public int hashCode() {
        return Expense.class.hashCode();
    }
}
