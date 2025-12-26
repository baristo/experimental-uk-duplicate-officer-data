package com.ukduplicateofficerdata.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukduplicateofficerdata.dto.OfficerRecordDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DuplicateOfficerAnalyserServiceImpl implements DuplicateOfficerAnalyserService {

    private static final Logger log = LoggerFactory.getLogger(DuplicateOfficerAnalyserServiceImpl.class);

    @Value("${anthropic.api.key}")
    private String anthropicApiKey;

    @Value("${anthropic.api.base-url}")
    private String anthropicBaseUrl;

    @Value("${anthropic.haiku.model}")
    private String anthropicHaikuModel;

    private final WebClient webClient;

    public DuplicateOfficerAnalyserServiceImpl(WebClient.Builder webClientBuilder,
                                                @Value("${anthropic.api.base-url}") String baseUrl) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .filter((request, next) -> {
                    log.debug("Request URI: {}", request.url());
                    log.debug("Request Method: {}", request.method());
                    return next.exchange(request);
                })
                .build();
    }

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
            // Check Claude Code environment variables
            String useBedrock = System.getenv("CLAUDE_CODE_USE_BEDROCK");
            String skipBedrockAuth = System.getenv("CLAUDE_CODE_SKIP_BEDROCK_AUTH");

            boolean isUsingBedrock = "1".equals(useBedrock) || "true".equalsIgnoreCase(useBedrock);
            boolean isSkippingAuth = "1".equals(skipBedrockAuth) || "true".equalsIgnoreCase(skipBedrockAuth);

            String endpoint;
            Map<String, Object> requestBody = new HashMap<>();

            log.debug("CLAUDE_CODE_USE_BEDROCK: {}", useBedrock);
            log.debug("CLAUDE_CODE_SKIP_BEDROCK_AUTH: {}", skipBedrockAuth);
            log.debug("Using Bedrock with proxy auth: {}", isUsingBedrock && isSkippingAuth);

            // Proxy expects model in URL path, NOT in request body
            endpoint = "/" + anthropicHaikuModel;
            // Do NOT include model in body - proxy extracts from URL and gets confused
            requestBody.put("max_tokens", 1024);
            requestBody.put("messages", List.of(
                    Map.of("role", "user", "content", prompt)
            ));

            log.debug("Calling Claude API at: {}{}", anthropicBaseUrl, endpoint);
            log.debug("Request body: {}", requestBody);
            log.debug("Request body model: {}", requestBody.get("model"));
            log.debug("Request body max_tokens: {}", requestBody.get("max_tokens"));
            log.debug("Request body messages size: {}", ((List<?>) requestBody.get("messages")).size());

            // Make API call - first get raw response as String with full response details
            String rawResponse = webClient.post()
                    .uri(endpoint)
                    .header("Authorization", "Bearer " + anthropicApiKey)
                    .header("content-type", "application/json")
                    .header("anthropic-version", "2023-06-01")
                    .header("x-model-id", anthropicHaikuModel)
                    .bodyValue(requestBody)
                    .exchangeToMono(clientResponse -> {
                        log.debug("HTTP Status: {}", clientResponse.statusCode());
                        log.debug("Response Headers: {}", clientResponse.headers().asHttpHeaders());
                        return clientResponse.bodyToMono(String.class)
                                .doOnNext(body -> log.debug("Response Body: {}", body))
                                .map(body -> body);
                    })
                    .block();

            log.debug("Raw API response after block: {}", rawResponse);

            if (rawResponse == null || rawResponse.isEmpty()) {
                log.error("Received null or empty response from API");
                return "Error: Received empty response from API";
            }

            // Parse the response
            Map<String, Object> response;
            try {
                // For now, let's use a simple JSON parsing approach
                // You might need to add Jackson dependency or use another approach
                response = parseJsonResponse(rawResponse);
            } catch (Exception e) {
                log.error("Failed to parse JSON response: {}", e.getMessage());
                return "Error: Failed to parse API response - " + e.getMessage();
            }

            // Extract text from response - handle both formats
            if (response != null) {
                log.debug("Response keys: {}", response.keySet());
                log.debug("Full response: {}", response);

                // Try OpenAI/LiteLLM format first
                if (response.containsKey("choices")) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    if (!choices.isEmpty()) {
                        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                        if (message != null && message.containsKey("content")) {
                            return (String) message.get("content");
                        }
                    }
                }
                // Try Anthropic format
                if (response.containsKey("content")) {
                    List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
                    if (!content.isEmpty() && content.get(0).containsKey("text")) {
                        return (String) content.get(0).get("text");
                    }
                }
            }

            log.error("Unable to parse API response. Response was: {}", response);
            return "Error: Unable to parse API response";

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

    private Map<String, Object> parseJsonResponse(String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    }
}