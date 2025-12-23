package com.ukduplicateofficerdata.controller;

import com.ukduplicateofficerdata.dto.OfficerRecordDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UkDuplicateOfficerDataController {

    @PostMapping("/analyse")
    public Map<String, String> analyseDuplicateData(@RequestBody List<OfficerRecordDTO> officerRecords) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Analysing " + officerRecords.size() + " officer records");
        response.put("status", "success");
        return response;
    }
}