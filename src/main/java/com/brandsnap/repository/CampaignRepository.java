package com.brandsnap.repository;

import com.brandsnap.model.Campaign;
import com.brandsnap.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByProject(Project project);
}
