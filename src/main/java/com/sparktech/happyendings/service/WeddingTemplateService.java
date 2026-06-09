package com.sparktech.happyendings.service;

import com.sparktech.happyendings.model.WeddingTemplate;
import com.sparktech.happyendings.model.UserTemplateFavorite;
import com.sparktech.happyendings.repository.WeddingTemplateRepository;
import com.sparktech.happyendings.repository.UserTemplateFavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WeddingTemplateService {

    @Autowired
    private WeddingTemplateRepository templateRepository;

    @Autowired
    private UserTemplateFavoriteRepository favoriteRepository;

    @Autowired
    private ActionLogService actionLogService;

    @Autowired
    private RedisService redisService;

    public List<WeddingTemplate> getActiveTemplates() {
        String cacheKey = "marketplace:templates:active";
        List<WeddingTemplate> cached = redisService.get(cacheKey, List.class);
        if (cached != null) {
            return cached;
        }

        List<WeddingTemplate> templates = templateRepository.findByStatus("ACTIVE");
        redisService.set(cacheKey, templates, 600); // 10 min cache
        return templates;
    }

    public Optional<WeddingTemplate> getTemplateById(Long id) {
        return templateRepository.findById(id);
    }

    @Transactional
    public WeddingTemplate createTemplate(WeddingTemplate template, Long actorUserId) {
        template.setPublishedAt(LocalDateTime.now());
        template.setStatus("ACTIVE");
        WeddingTemplate saved = templateRepository.save(template);

        redisService.delete("marketplace:templates:active");
        actionLogService.logAction(actorUserId, "TEMPLATE_PUBLISHED", "Published new template: " + template.getName());
        return saved;
    }

    @Transactional
    public WeddingTemplate cloneTemplate(Long templateId, String newName, Long actorUserId) {
        WeddingTemplate source = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Source template not found."));

        WeddingTemplate clone = new WeddingTemplate();
        clone.setName(newName);
        clone.setDescription(source.getDescription());
        clone.setPreviewImageUrl(source.getPreviewImageUrl());
        clone.setCategory(source.getCategory());
        clone.setTags(source.getTags());
        clone.setVersion("1.0.0-clone");
        clone.setStatus("ACTIVE");
        clone.setClonedFromId(templateId);
        clone.setPublishedAt(LocalDateTime.now());

        WeddingTemplate saved = templateRepository.save(clone);

        redisService.delete("marketplace:templates:active");
        actionLogService.logAction(actorUserId, "TEMPLATE_CLONED", "Cloned template " + templateId + " as " + newName);
        return saved;
    }

    @Transactional
    public void deprecateTemplate(Long templateId, Long actorUserId) {
        WeddingTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found."));
        template.setStatus("DEPRECATED");
        templateRepository.save(template);

        redisService.delete("marketplace:templates:active");
        actionLogService.logAction(actorUserId, "TEMPLATE_DEPRECATED", "Deprecated template: " + templateId);
    }

    @Transactional
    public boolean toggleTemplateFavorite(Long templateId, Long userId) {
        Optional<UserTemplateFavorite> existing = favoriteRepository.findByUserIdAndTemplateId(userId, templateId);
        WeddingTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found."));

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            template.setFavoritesCount(Math.max(0, template.getFavoritesCount() - 1));
            templateRepository.save(template);
            actionLogService.logAction(userId, "TEMPLATE_UNFAVORITED", "Unfavorited template: " + templateId);
            return false;
        } else {
            UserTemplateFavorite fav = new UserTemplateFavorite();
            fav.setUserId(userId);
            fav.setTemplateId(templateId);
            favoriteRepository.save(fav);

            template.setFavoritesCount(template.getFavoritesCount() + 1);
            templateRepository.save(template);
            actionLogService.logAction(userId, "TEMPLATE_FAVORITED", "Favorited template: " + templateId);
            return true;
        }
    }

    public List<WeddingTemplate> getUserFavorites(Long userId) {
        List<UserTemplateFavorite> favs = favoriteRepository.findByUserId(userId);
        List<Long> ids = favs.stream().map(UserTemplateFavorite::getTemplateId).collect(Collectors.toList());
        return templateRepository.findAllById(ids);
    }
}
