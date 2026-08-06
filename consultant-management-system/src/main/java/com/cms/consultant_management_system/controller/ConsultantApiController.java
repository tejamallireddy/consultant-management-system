package com.cms.consultant_management_system.controller;

import com.cms.consultant_management_system.dto.PageResponse;
import com.cms.consultant_management_system.entity.Consultant;
import com.cms.consultant_management_system.service.ConsultantService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/consultants")
public class ConsultantApiController {

    private final ConsultantService service;

    public ConsultantApiController(ConsultantService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<Consultant> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction dir = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));

        return PageResponse.from(service.search(keyword, pageable));
    }

    @GetMapping("/{id}")
    public Consultant getOne(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Consultant> create(@Valid @RequestBody Consultant consultant) {
        Consultant saved = service.create(consultant);
        return ResponseEntity
                .created(URI.create("/api/consultants/" + saved.getId()))
                .body(saved);
    }

    @PutMapping("/{id}")
    public Consultant update(@PathVariable Long id,
                             @Valid @RequestBody Consultant consultant) {
        return service.update(id, consultant);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return Map.of(
                "total",        service.countTotal(),
                "active",       service.countActive(),
                "inactive",     service.countInactive(),
                "newThisMonth", service.countNewThisMonth()
        );
    }
}