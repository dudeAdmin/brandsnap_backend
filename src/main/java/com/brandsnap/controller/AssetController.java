package com.brandsnap.controller;

import com.brandsnap.model.Asset;
import com.brandsnap.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @PostMapping
    public ResponseEntity<Asset> generateAsset(@RequestBody Map<String, String> request) {
        Long campaignId = Long.parseLong(request.get("campaignId"));
        String prompt = request.get("prompt");
        String inputImage = request.get("inputImage"); // Optional

        return ResponseEntity.ok(assetService.generateAsset(campaignId, prompt, inputImage));
    }

    @GetMapping
    public ResponseEntity<List<Asset>> getAssets(@RequestParam Long campaignId) {
        return ResponseEntity.ok(assetService.getAssetsByCampaign(campaignId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Asset> updateAsset(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        return ResponseEntity.ok(assetService.updateAsset(id, prompt));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok().build();
    }
}
