package com.karthi.expense_tracker.dto;

import com.karthi.expense_tracker.entity.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount,

        @NotNull(message = "Type is required (INCOME or EXPENSE)")
        TransactionType type,

        @Size(max = 255, message = "Max 255 characters")
        String description,

        @NotNull(message = "Date is required (yyyy-MM-dd)")
        LocalDate date,

        @NotNull(message = "Category id is required")
        Long categoryId
) {}