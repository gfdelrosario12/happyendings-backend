package com.sparktech.happyendings.repository;

import com.sparktech.happyendings.model.ProgramSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramSegmentRepository extends JpaRepository<ProgramSegment, Long> {
}
