package com.karthi.expense_tracker.dto;

import com.karthi.expense_tracker.entity.TransactionType;
import java.math.BigDecimal;

public record MonthTypeTotal(Integer year, Integer month, TransactionType type, BigDecimal total) {}