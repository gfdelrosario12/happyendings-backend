package com.sparktech.happyendings.repository;

import com.sparktech.happyendings.model.AssignedPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignedPersonRepository extends JpaRepository<AssignedPerson, Long> {
}
