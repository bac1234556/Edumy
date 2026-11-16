package com.edumy.payout.repository;

import com.edumy.payout.model.EarningEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EarningEntryRepository extends JpaRepository<EarningEntry, Long> {
    List<EarningEntry> findByInstructorId(Integer instructorId);
    Page<EarningEntry> findByInstructorId(Integer instructorId, Pageable pageable);
    Page<EarningEntry> findByInstructorIdAndCourseId(Integer instructorId, Integer courseId, Pageable pageable);
    boolean existsByOrderIdAndCourseIdAndInstructorId(Integer orderId, Integer courseId, Integer instructorId);
}
