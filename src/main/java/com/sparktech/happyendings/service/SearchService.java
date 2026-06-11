package com.sparktech.happyendings.service;

import com.sparktech.happyendings.dto.SearchResult;
import com.sparktech.happyendings.dto.UserDto;
import com.sparktech.happyendings.model.Guest;
import com.sparktech.happyendings.model.Invitation;
import com.sparktech.happyendings.model.User;
import com.sparktech.happyendings.model.WeddingTemplate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public SearchResult globalSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new SearchResult();
        }

        String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";

        List<User> users = entityManager.createQuery(
                "SELECT u FROM User u WHERE LOWER(u.name) LIKE :kw OR LOWER(u.email) LIKE :kw OR LOWER(u.firstName) LIKE :kw OR LOWER(u.lastName) LIKE :kw", User.class)
                .setParameter("kw", likeKeyword)
                .setMaxResults(20)
                .getResultList();

        List<UserDto> userDtos = users.stream()
                .map(user -> new UserDto(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getGender(),
                        user.getAge(),
                        user.getRole() != null ? user.getRole().name() : null,
                        user.getFirstName(),
                        user.getLastName(),
                        user.getPhoneNumber(),
                        user.getProfilePhoto(),
                        user.getAccountStatus() != null ? user.getAccountStatus().name() : null
                ))
                .collect(Collectors.toList());

        List<Invitation> invitations = entityManager.createQuery(
                "SELECT i FROM Invitation i WHERE i.deleted = false AND (LOWER(i.title) LIKE :kw OR LOWER(i.slug) LIKE :kw OR LOWER(i.theme) LIKE :kw)", Invitation.class)
                .setParameter("kw", likeKeyword)
                .setMaxResults(20)
                .getResultList();

        List<Guest> guests = entityManager.createQuery(
                "SELECT g FROM Guest g WHERE LOWER(g.name) LIKE :kw OR LOWER(g.email) LIKE :kw OR LOWER(g.firstName) LIKE :kw OR LOWER(g.lastName) LIKE :kw", Guest.class)
                .setParameter("kw", likeKeyword)
                .setMaxResults(50)
                .getResultList();

        List<WeddingTemplate> templates = entityManager.createQuery(
                "SELECT t FROM WeddingTemplate t WHERE LOWER(t.name) LIKE :kw OR LOWER(t.tags) LIKE :kw OR LOWER(t.category) LIKE :kw", WeddingTemplate.class)
                .setParameter("kw", likeKeyword)
                .setMaxResults(20)
                .getResultList();

        return new SearchResult(userDtos, invitations, guests, templates);
    }
}
