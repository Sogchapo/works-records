package com.works.worksrecords.controller;

import com.works.worksrecords.model.WorksRecord;
import com.works.worksrecords.repository.WorksRecordRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final WorksRecordRepository worksRecordRepository;

    public ReportController(WorksRecordRepository worksRecordRepository) {
        this.worksRecordRepository = worksRecordRepository;
    }

    @GetMapping
    public String showReportsDashboard(Model model) {
        model.addAttribute("approvedCount", worksRecordRepository.countByStatus(WorksRecord.RecordStatus.APPROVED));
        model.addAttribute("pendingCount", worksRecordRepository.countByStatus(WorksRecord.RecordStatus.PENDING));
        model.addAttribute("totalCount", worksRecordRepository.count());

        model.addAttribute("records", worksRecordRepository.findAll());
        model.addAttribute("activeFilter", "All Records");
        return "reports/index";
    }

    @GetMapping("/approved")
    public String getApprovedPermits(Model model) {
        List<WorksRecord> records = worksRecordRepository.findByStatus(WorksRecord.RecordStatus.APPROVED);
        model.addAttribute("records", records);
        model.addAttribute("activeFilter", "Approved Permits");
        addCountsToModel(model);
        return "reports/index";
    }

    @GetMapping("/pending")
    public String getPendingPermits(Model model) {
        List<WorksRecord> records = worksRecordRepository.findByStatus(WorksRecord.RecordStatus.PENDING);
        model.addAttribute("records", records);
        model.addAttribute("activeFilter", "Pending Permits");
        addCountsToModel(model);
        return "reports/index";
    }

    @GetMapping("/monthly")
    public String getMonthlyReport(@RequestParam(value = "month", required = false) Integer month,
                                   @RequestParam(value = "year", required = false) Integer year,
                                   Model model) {
        LocalDate now = LocalDate.now();
        int selectedMonth = (month != null) ? month : now.getMonthValue();
        int selectedYear = (year != null) ? year : now.getYear();

        LocalDate start = LocalDate.of(selectedYear, selectedMonth, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        List<WorksRecord> records = worksRecordRepository.findByDateSubmittedBetween(start, end);

        model.addAttribute("records", records);
        model.addAttribute("activeFilter", "Monthly Report (" + start.getMonth() + " " + selectedYear + ")");
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("selectedYear", selectedYear);
        addCountsToModel(model);
        return "reports/index";
    }

    @GetMapping("/yearly")
    public String getYearlyReport(@RequestParam(value = "year", required = false) Integer year, Model model) {
        int selectedYear = (year != null) ? year : LocalDate.now().getYear();

        LocalDate start = LocalDate.of(selectedYear, 1, 1);
        LocalDate end = LocalDate.of(selectedYear, 12, 31);

        List<WorksRecord> records = worksRecordRepository.findByDateSubmittedBetween(start, end);

        model.addAttribute("records", records);
        model.addAttribute("activeFilter", "Yearly Report (" + selectedYear + ")");
        model.addAttribute("selectedYear", selectedYear);
        addCountsToModel(model);
        return "reports/index";
    }

    @GetMapping("/search")
    public String searchReports(@RequestParam(value = "query", required = false) String query, Model model) {
        List<WorksRecord> results;
        if (query != null && !query.trim().isEmpty()) {
            results = worksRecordRepository.searchRecords(query);
            model.addAttribute("activeFilter", "Search Results for: '" + query + "'");
        } else {
            results = worksRecordRepository.findAll();
            model.addAttribute("activeFilter", "All Records");
        }
        model.addAttribute("records", results);
        model.addAttribute("query", query);
        addCountsToModel(model);
        return "reports/index";
    }

    private void addCountsToModel(Model model) {
        model.addAttribute("approvedCount", worksRecordRepository.countByStatus(WorksRecord.RecordStatus.APPROVED));
        model.addAttribute("pendingCount", worksRecordRepository.countByStatus(WorksRecord.RecordStatus.PENDING));
        model.addAttribute("totalCount", worksRecordRepository.count());
    }
}