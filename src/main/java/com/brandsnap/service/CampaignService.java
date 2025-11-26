package com.brandsnap.service;

import com.brandsnap.model.Campaign;
import com.brandsnap.model.Project;
import com.brandsnap.repository.CampaignRepository;
import com.brandsnap.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CampaignService {
    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public Campaign createCampaign(Campaign campaign, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        campaign.setProject(project);
        return campaignRepository.save(campaign);
    }

    public List<Campaign> getCampaignsByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return campaignRepository.findByProject(project);
    }

    public Campaign getCampaignById(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
    }
}
