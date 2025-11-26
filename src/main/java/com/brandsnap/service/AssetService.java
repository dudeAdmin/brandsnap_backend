package com.brandsnap.service;

import com.brandsnap.model.Asset;
import com.brandsnap.model.Campaign;
import com.brandsnap.repository.AssetRepository;
import com.brandsnap.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class AssetService {
    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Value("${nano.banana.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Asset generateAsset(Long campaignId, String prompt, String inputImage) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        // Call Nano Banana API
        String imageData = callNanoBananaApi(prompt, inputImage);

        Asset asset = new Asset();
        asset.setCampaign(campaign);
        asset.setPrompt(prompt);
        asset.setImageData(imageData);

        return assetRepository.save(asset);
    }

    private String callNanoBananaApi(String prompt, String inputImage) {
        try {
            String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent";

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = new java.util.ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, Object>> parts = new java.util.ArrayList<>();

            // Add input image if provided (should be base64 encoded)
            if (inputImage != null && !inputImage.isEmpty()) {
                Map<String, Object> imagePart = new HashMap<>();
                Map<String, Object> inlineData = new HashMap<>();

                // Extract base64 data if it's a data URL
                String base64Data = inputImage;
                if (inputImage.startsWith("data:")) {
                    int commaIndex = inputImage.indexOf(",");
                    if (commaIndex != -1) {
                        base64Data = inputImage.substring(commaIndex + 1);
                    }
                }

                inlineData.put("mime_type", "image/jpeg");
                inlineData.put("data", base64Data);
                imagePart.put("inline_data", inlineData);
                parts.add(imagePart);
            }

            // Add text prompt
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);
            parts.add(textPart);

            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);
            System.out.println(requestBody);
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Make API call
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Extract image data from response
                Map<String, Object> responseBody = response.getBody();
                // print response body
                System.out.println(responseBody);
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");

                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> contentResponse = (Map<String, Object>) candidate.get("content");
                    List<Map<String, Object>> responseParts = (List<Map<String, Object>>) contentResponse.get("parts");

                    if (responseParts != null && !responseParts.isEmpty()) {
                        // Look for the part with inlineData (image)
                        for (Map<String, Object> responsePart : responseParts) {
                            Map<String, Object> responseInlineData = (Map<String, Object>) responsePart
                                    .get("inlineData");

                            if (responseInlineData != null) {
                                String base64Image = (String) responseInlineData.get("data");
                                String mimeType = (String) responseInlineData.get("mimeType");

                                // Return as data URL for display
                                return "data:" + mimeType + ";base64," + base64Image;
                            }
                        }
                    }
                }
            }

            // Fallback to placeholder if API call fails
            System.err.println("No image found in API response, using placeholder");
            // Return a simple base64 encoded placeholder image (1x1 gray pixel)
            return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

        } catch (Exception e) {
            System.err.println("Error calling Gemini API: " + e.getMessage());
            e.printStackTrace();
            // Return placeholder on error (1x1 gray pixel)
            return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        }
    }

    public List<Asset> getAssetsByCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        return assetRepository.findByCampaign(campaign);
    }

    public Asset updateAsset(Long assetId, String prompt) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        // Re-generate or edit
        String imageData = callNanoBananaApi(prompt, null);
        asset.setPrompt(prompt);
        asset.setImageData(imageData);

        return assetRepository.save(asset);
    }

    public void deleteAsset(Long assetId) {
        assetRepository.deleteById(assetId);
    }
}
