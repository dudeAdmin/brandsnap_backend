package com.brandsnap.repository;

import com.brandsnap.model.Project;
import com.brandsnap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByCreatedBy(User user);
}
