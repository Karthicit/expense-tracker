package com.karthi.expense_tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySpend(LocalDate date, BigDecimal amount) {}