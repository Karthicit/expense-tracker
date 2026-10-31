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


import com.karthi.expense_tracker.dto.MonthTypeTotal;
import com.karthi.expense_tracker.dto.TrendPoint;
import java.util.ArrayList;
import com.karthi.expense_tracker.dto.DailySpend;
import com.karthi.expense_tracker.dto.TransactionResponse;
import org.springframework.data.domain.PageRequest;
import java.util.HashMap;
import java.util.Map;
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


        /** Income and expense for each of the last N months (oldest first), with zeros for empty months. */
    public List<TrendPoint> trend(int months) {
        if (months < 1 || months > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "months must be between 1 and 12");
        }
        User user = currentUser.get();
        YearMonth end = YearMonth.now();
        YearMonth start = end.minusMonths(months - 1);

        List<MonthTypeTotal> rows = transactionRepository.monthlyTotals(
                user.getId(), start.atDay(1), end.atEndOfMonth());

        List<TrendPoint> result = new ArrayList<>();
        for (YearMonth ym = start; !ym.isAfter(end); ym = ym.plusMonths(1)) {
            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expense = BigDecimal.ZERO;
            for (MonthTypeTotal r : rows) {
                if (r.year() == ym.getYear() && r.month() == ym.getMonthValue()) {
                    if (r.type() == TransactionType.INCOME) {
                        income = r.total();
                    } else {
                        expense = r.total();
                    }
                }
            }
            result.add(new TrendPoint(ym.toString(), income, expense));
        }
        return result;
    }

        /** Spending for every day of the month (oldest first), with zero for days without spending. */
    public List<DailySpend> daily(String month) {
        User user = currentUser.get();
        YearMonth ym = parseMonth(month);

        List<DailySpend> rows = transactionRepository.dailyExpenses(
                user.getId(), TransactionType.EXPENSE, ym.atDay(1), ym.atEndOfMonth());

        Map<LocalDate, BigDecimal> byDate = new HashMap<>();
        for (DailySpend r : rows) {
            byDate.put(r.date(), r.amount());
        }

        List<DailySpend> result = new ArrayList<>();
        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            LocalDate date = ym.atDay(day);
            result.add(new DailySpend(date, byDate.getOrDefault(date, BigDecimal.ZERO)));
        }
        return result;
    }

    /** The biggest expenses of the month, largest first. */
    public List<TransactionResponse> topExpenses(String month, int limit) {
        if (limit < 1 || limit > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be between 1 and 20");
        }
        User user = currentUser.get();
        YearMonth ym = parseMonth(month);

        return transactionRepository
                .findByUserIdAndTypeAndTransactionDateBetweenOrderByAmountDesc(
                        user.getId(), TransactionType.EXPENSE, ym.atDay(1), ym.atEndOfMonth(),
                        PageRequest.of(0, limit))
                .stream()
                .map(t -> new TransactionResponse(
                        t.getId(), t.getAmount(), t.getType(), t.getDescription(),
                        t.getTransactionDate(), t.getCategory().getId(), t.getCategory().getName()))
                .toList();
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