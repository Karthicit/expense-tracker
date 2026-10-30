package com.karthi.expense_tracker.dto;

import java.math.BigDecimal;

public record MonthlySummaryResponse(
        String month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal savings,
        BigDecimal savingsRatePercent
) {}