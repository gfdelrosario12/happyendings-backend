package com.sparktech.happyendings.controller;

import com.sparktech.happyendings.dto.ApiResponse;
import com.sparktech.happyendings.model.WeddingTemplate;
import com.sparktech.happyendings.model.User;
import com.sparktech.happyendings.service.WeddingTemplateService;
import com.sparktech.happyendings.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
public class WeddingTemplateController {

    @Autowired
    private WeddingTemplateService templateService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WeddingTemplate>>> getTemplates() {
        List<WeddingTemplate> templates = templateService.getActiveTemplates();
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WeddingTemplate>> getTemplateById(@PathVariable Long id) {
        WeddingTemplate template = templateService.getTemplateById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found."));
        return ResponseEntity.ok(ApiResponse.success(template));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WeddingTemplate>> createTemplate(@RequestBody WeddingTemplate template) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User actor = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Actor not found."));

        if (!actor.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Only administrators can publish global templates."));
        }

        WeddingTemplate created = templateService.createTemplate(template, actor.getId());
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<ApiResponse<WeddingTemplate>> cloneTemplate(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newName = body.get("name");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User actor = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Actor not found."));

        WeddingTemplate cloned = templateService.cloneTemplate(id, newName, actor.getId());
        return ResponseEntity.ok(ApiResponse.success(cloned));
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<Boolean>> toggleFavorite(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User actor = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Actor not found."));

        boolean isFavorited = templateService.toggleTemplateFavorite(id, actor.getId());
        return ResponseEntity.ok(ApiResponse.success(isFavorited));
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<WeddingTemplate>>> getFavorites() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User actor = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Actor not found."));

        List<WeddingTemplate> list = templateService.getUserFavorites(actor.getId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deprecateTemplate(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User actor = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Actor not found."));

        if (!actor.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Only administrators can deprecate templates."));
        }

        templateService.deprecateTemplate(id, actor.getId());
        return ResponseEntity.ok(ApiResponse.success("Template deprecated successfully."));
    }
}
