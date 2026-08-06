package com.cms.consultant_management_system.repository;

import com.cms.consultant_management_system.entity.Consultant;
import com.cms.consultant_management_system.entity.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ConsultantRepository")
class ConsultantRepositoryTest {

    @Autowired
    private ConsultantRepository repository;

    @BeforeEach
    void seed() {
        repository.deleteAll();
        repository.save(new Consultant("John Doe", "john@email.com", "9876543210",
                "Java, Spring Boot", 5, Status.ACTIVE));
        repository.save(new Consultant("Jane Smith", "jane@email.com", "9876543211",
                "Angular, Java", 4, Status.ACTIVE));
        repository.save(new Consultant("Mike Brown", "mike@email.com", "9876543212",
                "Python, Django", 6, Status.INACTIVE));
    }

    @Test
    @DisplayName("search() matches on technology")
    void searchMatchesTechnology() {
        var result = repository.search("Java", PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("search() matches on name")
    void searchMatchesName() {
        var result = repository.search("Mike", PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("mike@email.com");
    }

    @Test
    @DisplayName("search() is case-insensitive")
    void searchIsCaseInsensitive() {
        assertThat(repository.search("java", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(repository.search("JAVA", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("search() returns nothing for an unmatched keyword")
    void searchReturnsEmptyForNoMatch() {
        assertThat(repository.search("COBOL", PageRequest.of(0, 10)).getContent()).isEmpty();
    }

    @Test
    @DisplayName("email existence checks ignore case")
    void emailChecksIgnoreCase() {
        assertThat(repository.existsByEmailIgnoreCase("JOHN@EMAIL.COM")).isTrue();
        assertThat(repository.existsByEmailIgnoreCase("nobody@email.com")).isFalse();
    }

    @Test
    @DisplayName("existsByEmailIgnoreCaseAndIdNot() excludes the consultant itself")
    void existsExcludingSelf() {
        Consultant john = repository.findByEmailIgnoreCase("john@email.com").orElseThrow();

        assertThat(repository.existsByEmailIgnoreCaseAndIdNot("john@email.com", john.getId())).isFalse();
        assertThat(repository.existsByEmailIgnoreCaseAndIdNot("jane@email.com", john.getId())).isTrue();
    }

    @Test
    @DisplayName("countByStatus() counts each status")
    void countByStatus() {
        assertThat(repository.countByStatus(Status.ACTIVE)).isEqualTo(2);
        assertThat(repository.countByStatus(Status.INACTIVE)).isEqualTo(1);
    }

    @Test
    @DisplayName("@CreationTimestamp populates createdAt on save")
    void createdAtIsPopulated() {
        Consultant saved = repository.save(new Consultant("New Person", "new@email.com",
                "9876543299", "Go", 2, Status.ACTIVE));
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("pagination splits results")
    void paginationSplitsResults() {
        var page0 = repository.findAll(PageRequest.of(0, 2));
        assertThat(page0.getContent()).hasSize(2);
        assertThat(page0.getTotalPages()).isEqualTo(2);
        assertThat(page0.isFirst()).isTrue();
    }
}