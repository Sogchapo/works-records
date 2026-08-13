package com.works.worksrecords.controller;

import com.works.worksrecords.model.WorksRecord;
import com.works.worksrecords.repository.WorksRecordRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    private final WorksRecordRepository worksRecordRepository;

    public DashboardController(WorksRecordRepository worksRecordRepository) {
        this.worksRecordRepository = worksRecordRepository;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        long totalRecords = worksRecordRepository.count();
        long addedToday = worksRecordRepository.countByDateSubmitted(LocalDate.now());
        long pendingRecords = worksRecordRepository.countByStatus(WorksRecord.RecordStatus.PENDING);
        long approvedRecords = worksRecordRepository.countByStatus(WorksRecord.RecordStatus.APPROVED);
        List<WorksRecord> recentUploads = worksRecordRepository.findTop5ByOrderByIdDesc();

        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("addedToday", addedToday);
        model.addAttribute("pendingRecords", pendingRecords);
        model.addAttribute("approvedRecords", approvedRecords);
        model.addAttribute("recentUploads", recentUploads);

        return "dashboard";
    }
}