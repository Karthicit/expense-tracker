package com.karthi.expense_tracker.service;

import com.karthi.expense_tracker.dto.CategoryReportResponse;
import com.karthi.expense_tracker.dto.CategorySpendResponse;
import com.karthi.expense_tracker.dto.CategoryTotal;
import com.karthi.expense_tracker.dto.MonthlySummaryResponse;
import com.karthi.expense_tracker.entity.TransactionType;
import com.karthi.expense_tracker.entity.User;
import com.karthi.expense_tracker.repository.TransactionRepository;
import com.karthi.expense_tracker.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final TransactionRepository transactionRepository;
    private final CurrentUser currentUser;

    public ReportService(TransactionRepository transactionRepository, CurrentUser currentUser) {
        this.transactionRepository = transactionRepository;
        this.currentUser = currentUser;
    }

    public MonthlySummaryResponse monthlySummary(String month) {
        User user = currentUser.get();
        YearMonth ym = parseMonth(month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        BigDecimal income = orZero(transactionRepository.sumAmount(user.getId(), TransactionType.INCOME, from, to));
        BigDecimal expense = orZero(transactionRepository.sumAmount(user.getId(), TransactionType.EXPENSE, from, to));
        BigDecimal savings = income.subtract(expense);

        // savings rate = savings / income * 100 (0 if no income, to avoid dividing by zero)
        BigDecimal rate = income.signum() == 0
                ? BigDecimal.ZERO.setScale(2)
                : savings.multiply(HUNDRED).divide(income, 2, RoundingMode.HALF_UP);

        return new MonthlySummaryResponse(ym.toString(), income, expense, savings, rate);
    }

    public CategoryReportResponse categoryWise(String month) {
        User user = currentUser.get();
        YearMonth ym = parseMonth(month);

        List<CategoryTotal> rows = transactionRepository.sumByCategory(
                user.getId(), TransactionType.EXPENSE, ym.atDay(1), ym.atEndOfMonth());

        BigDecimal totalExpense = rows.stream()
                .map(CategoryTotal::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategorySpendResponse> categories = rows.stream()
                .map(r -> new CategorySpendResponse(
                        r.category(),
                        r.total(),
                        totalExpense.signum() == 0
                                ? BigDecimal.ZERO.setScale(2)
                                : r.total().multiply(HUNDRED).divide(totalExpense, 2, RoundingMode.HALF_UP)))
                .toList();

        return new CategoryReportResponse(ym.toString(), totalExpense, categories);
    }

    // "2026-10" -> YearMonth; missing means the current month
    private YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month.trim());
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Month must be yyyy-MM, e.g. 2026-10");
        }
    }

    private BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}