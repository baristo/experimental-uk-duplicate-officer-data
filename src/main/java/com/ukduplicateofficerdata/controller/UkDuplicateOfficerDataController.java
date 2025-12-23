package com.ukduplicateofficerdata.controller;

import com.ukduplicateofficerdata.dto.OfficerRecordDTO;
import com.ukduplicateofficerdata.service.DuplicateOfficerAnalyserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UkDuplicateOfficerDataController {

    private final DuplicateOfficerAnalyserService duplicateOfficerAnalyserService;

    public UkDuplicateOfficerDataController(DuplicateOfficerAnalyserService duplicateOfficerAnalyserService) {
        this.duplicateOfficerAnalyserService = duplicateOfficerAnalyserService;
    }

    @PostMapping("/analyse")
    public Map<String, String> analyseDuplicateData(@RequestBody List<OfficerRecordDTO> officerRecords) {
        return duplicateOfficerAnalyserService.analyse(officerRecords);
    }
}