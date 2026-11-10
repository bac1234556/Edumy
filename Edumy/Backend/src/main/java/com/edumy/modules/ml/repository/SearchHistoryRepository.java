package com.edumy.modules.ml.repository;

import com.edumy.modules.ml.model.SearchHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Integer> {
}
