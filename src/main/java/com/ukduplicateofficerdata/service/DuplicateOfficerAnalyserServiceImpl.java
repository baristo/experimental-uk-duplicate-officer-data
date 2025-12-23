package com.ukduplicateofficerdata.service;

import com.anthropic.Anthropic;
import com.anthropic.models.MessageCreateParams;
import com.anthropic.models.MessageResponse;
import com.ukduplicateofficerdata.dto.OfficerRecordDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DuplicateOfficerAnalyserServiceImpl implements DuplicateOfficerAnalyserService {

    @Value("${anthropic.api.key}")
    private String anthropicApiKey;

    @Override
    public Map<String, String> analyse(List<OfficerRecordDTO> officerRecords) {
        if (officerRecords == null || officerRecords.isEmpty()) {
            Map<String, String> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "No officer records provided");
            return result;
        }

        if (officerRecords.size() == 1) {
            Map<String, String> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Only one record provided, no duplicates to analyse");
            result.put("mostLikelyCorrectRecord", "0");
            return result;
        }

        String prompt = buildAnalysisPrompt(officerRecords);
        String analysisResult = callClaudeHaiku(prompt);

        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Analysed " + officerRecords.size() + " officer records");
        result.put("analysis", analysisResult);
        result.put("mostLikelyCorrectRecord", extractMostLikelyRecordIndex(analysisResult));

        return result;
    }

    private String buildAnalysisPrompt(List<OfficerRecordDTO> officerRecords) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are analyzing duplicate officer records from UK Companies House data. ");
        prompt.append("Your task is to determine which record is most likely the latest and most accurate.\n\n");
        prompt.append("Consider the following factors:\n");
        prompt.append("1. Most recent appointment or resignation dates\n");
        prompt.append("2. Completeness of data (more filled fields suggest more recent/accurate)\n");
        prompt.append("3. Data consistency and validity\n");
        prompt.append("4. Address information completeness\n\n");
        prompt.append("Here are the officer records to analyze:\n\n");

        for (int i = 0; i < officerRecords.size(); i++) {
            OfficerRecordDTO record = officerRecords.get(i);
            prompt.append("Record ").append(i).append(":\n");
            prompt.append("  Company Number: ").append(record.getCompanyNumber()).append("\n");
            prompt.append("  Person Number: ").append(record.getPersonNumber()).append("\n");
            prompt.append("  Name: ").append(record.getTitle()).append(" ")
                    .append(record.getForenames()).append(" ").append(record.getSurname()).append("\n");
            prompt.append("  Honours: ").append(record.getHonours()).append("\n");
            prompt.append("  Appointment Date: ").append(record.getAppointmentDate()).append("\n");
            prompt.append("  Resignation Date: ").append(record.getResignationDate()).append("\n");
            prompt.append("  Date of Birth: ").append(record.getDateOfBirth()).append("\n");
            prompt.append("  Nationality: ").append(record.getNationality()).append("\n");
            prompt.append("  Occupation: ").append(record.getOccupation()).append("\n");
            prompt.append("  Usual Residential Country: ").append(record.getUsualResidentialCountry()).append("\n");
            prompt.append("  Address:\n");
            prompt.append("    Care Of: ").append(record.getCareOf()).append("\n");
            prompt.append("    PO Box: ").append(record.getPoBox()).append("\n");
            prompt.append("    Address Line 1: ").append(record.getAddressLine1()).append("\n");
            prompt.append("    Address Line 2: ").append(record.getAddressLine2()).append("\n");
            prompt.append("    Post Town: ").append(record.getPostTown()).append("\n");
            prompt.append("    County: ").append(record.getCounty()).append("\n");
            prompt.append("    Country: ").append(record.getCountry()).append("\n");
            prompt.append("    Post Code: ").append(record.getPostCode()).append("\n");
            prompt.append("\n");
        }

        prompt.append("Please analyze these records and determine which one is most likely the latest and most accurate. ");
        prompt.append("Provide your reasoning and conclude with: 'Most likely correct record: [index]' where index is 0, 1, 2, etc.");

        return prompt.toString();
    }

    private String callClaudeHaiku(String prompt) {
        try {
            Anthropic client = Anthropic.builder()
                    .apiKey(anthropicApiKey)
                    .build();

            MessageCreateParams params = MessageCreateParams.builder()
                    .model("claude-3-haiku-20240307")
                    .maxTokens(1024)
                    .addMessage(MessageCreateParams.Message.builder()
                            .role(MessageCreateParams.Message.Role.USER)
                            .content(prompt)
                            .build())
                    .build();

            MessageResponse response = client.messages().create(params);
            return response.content().get(0).text();

        } catch (Exception e) {
            return "Error calling Claude API: " + e.getMessage();
        }
    }

    private String extractMostLikelyRecordIndex(String analysisResult) {
        String[] lines = analysisResult.split("\n");
        for (String line : lines) {
            if (line.toLowerCase().contains("most likely correct record:")) {
                String[] parts = line.split(":");
                if (parts.length > 1) {
                    return parts[1].trim().replaceAll("[^0-9]", "");
                }
            }
        }
        return "0";
    }
}