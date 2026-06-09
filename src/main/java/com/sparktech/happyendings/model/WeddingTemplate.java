package com.sparktech.happyendings.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class WeddingTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String previewImageUrl;
    private String category; // e.g. Classic, Modern, Rustic
    private String tags; // Comma-separated tags e.g. "floral, elegant, clean"
    private String version = "1.0.0";
    private String status = "ACTIVE"; // ACTIVE, DEPRECATED, DRAFT
    private Long clonedFromId;
    private int favoritesCount = 0;
    private int viewsCount = 0;
    private LocalDateTime publishedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPreviewImageUrl() { return previewImageUrl; }
    public void setPreviewImageUrl(String previewImageUrl) { this.previewImageUrl = previewImageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getClonedFromId() { return clonedFromId; }
    public void setClonedFromId(Long clonedFromId) { this.clonedFromId = clonedFromId; }

    public int getFavoritesCount() { return favoritesCount; }
    public void setFavoritesCount(int favoritesCount) { this.favoritesCount = favoritesCount; }

    public int getViewsCount() { return viewsCount; }
    public void setViewsCount(int viewsCount) { this.viewsCount = viewsCount; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
