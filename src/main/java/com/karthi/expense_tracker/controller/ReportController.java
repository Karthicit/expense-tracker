package com.karthi.expense_tracker.controller;

import com.karthi.expense_tracker.dto.CategoryReportResponse;
import com.karthi.expense_tracker.dto.MonthlySummaryResponse;
import com.karthi.expense_tracker.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}