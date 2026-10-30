package com.karthi.expense_tracker.dto;

import java.math.BigDecimal;
import java.util.List;

public record CategoryReportResponse(String month, BigDecimal totalExpense, List<CategorySpendResponse> categories) {}