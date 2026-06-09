package com.sparktech.happyendings.repository;

import com.sparktech.happyendings.model.UserTemplateFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserTemplateFavoriteRepository extends JpaRepository<UserTemplateFavorite, Long> {
    List<UserTemplateFavorite> findByUserId(Long userId);
    Optional<UserTemplateFavorite> findByUserIdAndTemplateId(Long userId, Long templateId);
    long countByTemplateId(Long templateId);
}
