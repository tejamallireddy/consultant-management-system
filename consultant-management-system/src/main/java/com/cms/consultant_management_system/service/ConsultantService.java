package com.cms.consultant_management_system.service;

import com.cms.consultant_management_system.entity.Consultant;
import com.cms.consultant_management_system.entity.Status;
import com.cms.consultant_management_system.exception.ConsultantNotFoundException;
import com.cms.consultant_management_system.exception.DuplicateEmailException;
import com.cms.consultant_management_system.repository.ConsultantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConsultantService {

    private final ConsultantRepository repository;

    // Constructor injection - no @Autowired needed for a single constructor
    public ConsultantService(ConsultantRepository repository) {
        this.repository = repository;
    }

    public Page<Consultant> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Consultant> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return repository.findAll(pageable);
        }
        return repository.search(keyword.trim(), pageable);
    }

    public Consultant findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ConsultantNotFoundException(id));
    }

    @Transactional
    public Consultant create(Consultant consultant) {
        if (repository.existsByEmailIgnoreCase(consultant.getEmail())) {
            throw new DuplicateEmailException(consultant.getEmail());
        }
        consultant.setId(null);   // guard against a client-supplied id
        return repository.save(consultant);
    }

    @Transactional
    public Consultant update(Long id, Consultant incoming) {
        Consultant existing = findById(id);

        if (repository.existsByEmailIgnoreCaseAndIdNot(incoming.getEmail(), id)) {
            throw new DuplicateEmailException(incoming.getEmail());
        }

        existing.setName(incoming.getName());
        existing.setEmail(incoming.getEmail());
        existing.setPhone(incoming.getPhone());
        existing.setTechnology(incoming.getTechnology());
        existing.setExperience(incoming.getExperience());
        existing.setStatus(incoming.getStatus());

        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ConsultantNotFoundException(id);
        }
        repository.deleteById(id);
    }

    // ---- Dashboard stats (Part C) ----

    public long countTotal() {
        return repository.count();
    }

    public long countActive() {
        return repository.countByStatus(Status.ACTIVE);
    }

    public long countInactive() {
        return repository.countByStatus(Status.INACTIVE);
    }

    public long countNewThisMonth() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return repository.countByCreatedAtAfter(startOfMonth);
    }

    public List<Consultant> findLatest(int count) {
        return repository.findAll(
                PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "id"))
        ).getContent();
    }

}