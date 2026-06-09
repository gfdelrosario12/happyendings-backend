package com.sparktech.happyendings.repository;

import com.sparktech.happyendings.model.WeddingTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WeddingTemplateRepository extends JpaRepository<WeddingTemplate, Long> {
    List<WeddingTemplate> findByStatus(String status);
    List<WeddingTemplate> findByCategory(String category);
}
