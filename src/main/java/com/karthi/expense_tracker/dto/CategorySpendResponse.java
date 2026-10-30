package com.karthi.expense_tracker.dto;

import java.math.BigDecimal;

public record CategorySpendResponse(String category, BigDecimal total, BigDecimal percentage) {}