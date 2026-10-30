package com.karthi.expense_tracker.dto;

import com.karthi.expense_tracker.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        TransactionType type,
        String description,
        LocalDate date,
        Long categoryId,
        String categoryName
) {}