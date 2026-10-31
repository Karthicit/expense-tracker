package com.karthi.expense_tracker.dto;

import java.math.BigDecimal;

public record TrendPoint(String month, BigDecimal income, BigDecimal expense) {}