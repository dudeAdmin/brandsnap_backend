package com.brandsnap.controller;

import com.brandsnap.model.Project;
import com.brandsnap.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project, @RequestParam Long userId) {
        return ResponseEntity.ok(projectService.createProject(project, userId));
    }

    @GetMapping
    public ResponseEntity<List<Project>> getProjects(@RequestParam Long userId) {
        return ResponseEntity.ok(projectService.getProjectsByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
