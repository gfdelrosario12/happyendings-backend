package com.sparktech.happyendings.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class WeddingTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String tags;
    private String category;
    private String description;
    private String previewImageUrl;
    private String status;
    private LocalDateTime publishedAt;
    private Long clonedFromId;
    private long favoritesCount;
    private String templateVersion; // For semantic versioning like "1.0.0"

    @Version
    private Long version; // For JPA Optimistic Locking

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPreviewImageUrl() {
        return previewImageUrl;
    }

    public void setPreviewImageUrl(String previewImageUrl) {
        this.previewImageUrl = previewImageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Long getClonedFromId() {
        return clonedFromId;
    }

    public void setClonedFromId(Long clonedFromId) {
        this.clonedFromId = clonedFromId;
    }

    public long getFavoritesCount() {
        return favoritesCount;
    }

    public void setFavoritesCount(long favoritesCount) {
        this.favoritesCount = favoritesCount;
    }

    public String getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
