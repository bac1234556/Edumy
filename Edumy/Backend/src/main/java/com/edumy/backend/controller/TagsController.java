package com.edumy.backend.controller;

import com.edumy.backend.model.*;
import com.edumy.backend.dto.*;
import com.edumy.backend.repository.*;
import com.edumy.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.net.URI;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/Tags")
public class TagsController {

    @Autowired
    private TagRepository tagRepository;

    @GetMapping
    public ResponseEntity<?> getTags() {
        List<Tag> tags = tagRepository.findAll();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchTags(@RequestParam(required = false) String query) {
        if (query == null || query.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<Tag>());
        }
        
        List<Tag> tags = tagRepository.findTop20ByNameContainingIgnoreCase(query);
        return ResponseEntity.ok(tags);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> createTag(@RequestBody Tag tag) {
        Tag existing = tagRepository.findByNameIgnoreCase(tag.getName());
        if (existing != null) {
            return ResponseEntity.ok(existing);
        }
        
        Tag savedTag = tagRepository.save(tag);
        return ResponseEntity.created(URI.create("/api/Tags/" + savedTag.getId())).body(savedTag);
    }
}
