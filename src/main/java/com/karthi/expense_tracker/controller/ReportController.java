package com.karthi.expense_tracker.controller;

import com.karthi.expense_tracker.dto.CategoryReportResponse;
import com.karthi.expense_tracker.dto.MonthlySummaryResponse;
import com.karthi.expense_tracker.dto.TrendPoint;
import com.karthi.expense_tracker.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.karthi.expense_tracker.dto.DailySpend;
import com.karthi.expense_tracker.dto.TransactionResponse;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public MonthlySummaryResponse monthly(@RequestParam(required = false) String month) {
        return reportService.monthlySummary(month);
    }

    @GetMapping("/category-wise")
    public CategoryReportResponse categoryWise(@RequestParam(required = false) String month) {
        return reportService.categoryWise(month);
    }

    // NEW: GET /api/reports/trend?months=6
    @GetMapping("/trend")
    public List<TrendPoint> trend(@RequestParam(defaultValue = "6") int months) {
        return reportService.trend(months);
        
    }
        // GET /api/reports/daily?month=2026-10
    @GetMapping("/daily")
    public List<DailySpend> daily(@RequestParam(required = false) String month) {
        return reportService.daily(month);
    }

    // GET /api/reports/top-expenses?month=2026-10&limit=5
    @GetMapping("/top-expenses")
    public List<TransactionResponse> topExpenses(@RequestParam(required = false) String month,
                                                 @RequestParam(defaultValue = "5") int limit) {
        return reportService.topExpenses(month, limit);
    }
}