package com.ukduplicateofficerdata.service;

import com.ukduplicateofficerdata.dto.OfficerRecordDTO;

import java.util.List;
import java.util.Map;

public interface DuplicateOfficerAnalyserService {
    Map<String, String> analyse(List<OfficerRecordDTO> officerRecords);
}