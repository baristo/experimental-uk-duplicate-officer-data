package com.ukduplicateofficerdata.controller;

import com.ukduplicateofficerdata.dto.OfficerRecordDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UkDuplicateOfficerDataController {

    @PostMapping("/analyse")
    public Map<String, String> analyseDuplicateData(@RequestBody OfficerRecordDTO officerRecord) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Analysing officer: " + officerRecord.getForenames() + " " + officerRecord.getSurname());
        response.put("status", "success");
        return response;
    }
}