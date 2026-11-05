package com.edumy.backend.repository;

import com.edumy.backend.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    Tag findByName(String name);
    List<Tag> findTop20ByNameContainingIgnoreCase(String name);
    Tag findByNameIgnoreCase(String name);
}
