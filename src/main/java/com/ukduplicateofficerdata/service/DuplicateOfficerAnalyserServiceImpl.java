package com.ukduplicateofficerdata.service;

import com.ukduplicateofficerdata.dto.OfficerRecordDTO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DuplicateOfficerAnalyserServiceImpl implements DuplicateOfficerAnalyserService {

    @Override
    public Map<String, String> analyse(List<OfficerRecordDTO> officerRecords) {
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Analysed " + officerRecords.size() + " officer records");
        return result;
    }
}