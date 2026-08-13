package com.works.worksrecords.controller;

import com.works.worksrecords.model.RecordDocument;
import com.works.worksrecords.model.WorksRecord;
import com.works.worksrecords.repository.WorksRecordRepository;
import com.works.worksrecords.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/records")
public class WorksRecordController {

    private final WorksRecordRepository worksRecordRepository;
    private final FileStorageService fileStorageService;

    public WorksRecordController(WorksRecordRepository worksRecordRepository, FileStorageService fileStorageService) {
        this.worksRecordRepository = worksRecordRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("record", new WorksRecord());
        return "records/register";
    }

    @PostMapping("/save")
    public String saveRecord(@ModelAttribute WorksRecord record,
                             @RequestParam(value = "files", required = false) MultipartFile[] files,
                             @RequestParam(value = "categories", required = false) String[] categories) {

        if (record.getStatus() == null) {
            record.setStatus(WorksRecord.RecordStatus.PENDING);
        }

        WorksRecord savedRecord = worksRecordRepository.save(record);

        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                if (!file.isEmpty()) {
                    String storedFileName = fileStorageService.storeFile(file);
                    RecordDocument doc = new RecordDocument();
                    doc.setFileName(file.getOriginalFilename());
                    doc.setFileType(file.getContentType());
                    doc.setFilePath("/uploads/" + storedFileName);
                    doc.setDocumentCategory(categories != null && i < categories.length ? categories[i] : "Attachment");
                    doc.setWorksRecord(savedRecord);
                    savedRecord.getDocuments().add(doc);
                }
            }
            worksRecordRepository.save(savedRecord);
        }

        return "redirect:/records/view/" + savedRecord.getId();
    }

    @GetMapping("/search")
    public String searchRecords(@RequestParam(value = "query", required = false) String query, Model model) {
        List<WorksRecord> results;
        if (query != null && !query.trim().isEmpty()) {
            results = worksRecordRepository.searchRecords(query);
        } else {
            results = worksRecordRepository.findAll();
        }
        model.addAttribute("records", results);
        model.addAttribute("query", query);
        return "records/search";
    }

    @GetMapping("/view/{id}")
    public String viewRecord(@PathVariable Long id, Model model) {
        WorksRecord record = worksRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Record ID: " + id));
        model.addAttribute("record", record);
        return "records/view";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        WorksRecord record = worksRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Record ID: " + id));
        model.addAttribute("record", record);
        return "records/edit";
    }

    @PostMapping("/update/{id}")
    public String updateRecord(@PathVariable Long id,
                               @ModelAttribute WorksRecord updatedRecord,
                               @RequestParam(value = "files", required = false) MultipartFile[] files,
                               @RequestParam(value = "categories", required = false) String[] categories) {

        WorksRecord existingRecord = worksRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Record ID: " + id));

        existingRecord.setPropertyOwnerName(updatedRecord.getPropertyOwnerName());
        existingRecord.setApplicantName(updatedRecord.getApplicantName());
        existingRecord.setContactNumber(updatedRecord.getContactNumber());
        existingRecord.setPlotNumber(updatedRecord.getPlotNumber());
        existingRecord.setDigitalAddress(updatedRecord.getDigitalAddress());
        existingRecord.setGpsCoordinates(updatedRecord.getGpsCoordinates());
        existingRecord.setPermitNumber(updatedRecord.getPermitNumber());
        existingRecord.setBuildingPermitNumber(updatedRecord.getBuildingPermitNumber());
        existingRecord.setApplicationNumber(updatedRecord.getApplicationNumber());
        existingRecord.setLandTitleNumber(updatedRecord.getLandTitleNumber());
        existingRecord.setRegion(updatedRecord.getRegion());
        existingRecord.setDistrict(updatedRecord.getDistrict());
        existingRecord.setTown(updatedRecord.getTown());
        existingRecord.setElectoralArea(updatedRecord.getElectoralArea());
        existingRecord.setStreetName(updatedRecord.getStreetName());
        existingRecord.setDevelopmentType(updatedRecord.getDevelopmentType());
        existingRecord.setStatus(updatedRecord.getStatus());

        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                if (!file.isEmpty()) {
                    String storedFileName = fileStorageService.storeFile(file);
                    RecordDocument doc = new RecordDocument();
                    doc.setFileName(file.getOriginalFilename());
                    doc.setFileType(file.getContentType());
                    doc.setFilePath("/uploads/" + storedFileName);
                    doc.setDocumentCategory(categories != null && i < categories.length ? categories[i] : "Attachment");
                    doc.setWorksRecord(existingRecord);
                    existingRecord.getDocuments().add(doc);
                }
            }
        }

        worksRecordRepository.save(existingRecord);
        return "redirect:/records/view/" + id;
    }

    @GetMapping("/delete/{id}")
    public String deleteRecord(@PathVariable Long id) {
        worksRecordRepository.deleteById(id);
        return "redirect:/";
    }

    // --- NEW FILE DOWNLOAD ENDPOINT ---
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            // Path where FileStorageService stores uploaded files
            Path filePath = Paths.get("uploads").resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}