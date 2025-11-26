package com.brandsnap.controller;

import com.brandsnap.model.Campaign;
import com.brandsnap.service.CampaignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    @Autowired
    private CampaignService campaignService;

    @PostMapping
    public ResponseEntity<Campaign> createCampaign(@RequestBody Campaign campaign, @RequestParam Long projectId) {
        return ResponseEntity.ok(campaignService.createCampaign(campaign, projectId));
    }

    @GetMapping
    public ResponseEntity<List<Campaign>> getCampaigns(@RequestParam Long projectId) {
        return ResponseEntity.ok(campaignService.getCampaignsByProject(projectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Campaign> getCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getCampaignById(id));
    }
}
