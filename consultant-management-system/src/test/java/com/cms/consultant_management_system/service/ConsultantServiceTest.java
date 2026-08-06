package com.cms.consultant_management_system.service;

import com.cms.consultant_management_system.entity.Consultant;
import com.cms.consultant_management_system.entity.Status;
import com.cms.consultant_management_system.exception.ConsultantNotFoundException;
import com.cms.consultant_management_system.exception.DuplicateEmailException;
import com.cms.consultant_management_system.repository.ConsultantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsultantService")
class ConsultantServiceTest {

    @Mock
    private ConsultantRepository repository;

    @InjectMocks
    private ConsultantService service;

    private Consultant sample;

    @BeforeEach
    void setUp() {
        sample = new Consultant("John Doe", "john@email.com", "9876543210",
                "Java, Spring Boot", 5, Status.ACTIVE);
        sample.setId(1L);
    }

    @Test
    @DisplayName("create() saves when the email is free")
    void createSavesWhenEmailIsFree() {
        when(repository.existsByEmailIgnoreCase("john@email.com")).thenReturn(false);
        when(repository.save(any(Consultant.class))).thenReturn(sample);

        Consultant result = service.create(sample);

        assertThat(result.getName()).isEqualTo("John Doe");
        verify(repository).save(any(Consultant.class));
    }

    @Test
    @DisplayName("create() rejects a duplicate email without touching the database")
    void createRejectsDuplicateEmail() {
        when(repository.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.create(sample))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("john@email.com");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create() clears any client-supplied id")
    void createClearsSuppliedId() {
        when(repository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(repository.save(any(Consultant.class))).thenAnswer(inv -> inv.getArgument(0));

        Consultant incoming = new Consultant("Jane", "jane@email.com", "9876543211",
                "Angular", 4, Status.ACTIVE);
        incoming.setId(999L);

        service.create(incoming);

        ArgumentCaptor<Consultant> captor = ArgumentCaptor.forClass(Consultant.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
    }

    @Test
    @DisplayName("findById() throws when the id does not exist")
    void findByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ConsultantNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("update() copies fields onto the managed entity")
    void updateCopiesFields() {
        when(repository.findById(1L)).thenReturn(Optional.of(sample));
        when(repository.existsByEmailIgnoreCaseAndIdNot("new@email.com", 1L)).thenReturn(false);
        when(repository.save(any(Consultant.class))).thenAnswer(inv -> inv.getArgument(0));

        Consultant incoming = new Consultant("John Updated", "new@email.com", "9999999999",
                "Kafka", 8, Status.INACTIVE);

        Consultant result = service.update(1L, incoming);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Updated");
        assertThat(result.getExperience()).isEqualTo(8);
        assertThat(result.getStatus()).isEqualTo(Status.INACTIVE);
    }

    @Test
    @DisplayName("update() allows a consultant to keep its own email")
    void updateAllowsOwnEmail() {
        when(repository.findById(1L)).thenReturn(Optional.of(sample));
        when(repository.existsByEmailIgnoreCaseAndIdNot("john@email.com", 1L)).thenReturn(false);
        when(repository.save(any(Consultant.class))).thenAnswer(inv -> inv.getArgument(0));

        Consultant incoming = new Consultant("John Doe", "john@email.com", "9876543210",
                "Java", 6, Status.ACTIVE);

        assertThat(service.update(1L, incoming).getExperience()).isEqualTo(6);
    }

    @Test
    @DisplayName("update() rejects an email owned by someone else")
    void updateRejectsSomeoneElsesEmail() {
        when(repository.findById(1L)).thenReturn(Optional.of(sample));
        when(repository.existsByEmailIgnoreCaseAndIdNot("taken@email.com", 1L)).thenReturn(true);

        Consultant incoming = new Consultant("John", "taken@email.com", "9876543210",
                "Java", 5, Status.ACTIVE);

        assertThatThrownBy(() -> service.update(1L, incoming))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    @DisplayName("delete() throws when the id does not exist")
    void deleteThrowsWhenMissing() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ConsultantNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("search() falls back to findAll for a blank keyword")
    void searchFallsBackForBlankKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Consultant> page = new PageImpl<>(List.of(sample));
        when(repository.findAll(pageable)).thenReturn(page);

        assertThat(service.search("   ", pageable).getContent()).hasSize(1);

        verify(repository).findAll(pageable);
        verify(repository, never()).search(anyString(), any());
    }

    @Test
    @DisplayName("search() trims the keyword before querying")
    void searchTrimsKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.search(eq("Java"), any())).thenReturn(new PageImpl<>(List.of(sample)));

        service.search("  Java  ", pageable);

        verify(repository).search("Java", pageable);
    }

    @Test
    @DisplayName("dashboard counters delegate to the repository")
    void dashboardCountersDelegate() {
        when(repository.count()).thenReturn(12L);
        when(repository.countByStatus(Status.ACTIVE)).thenReturn(10L);
        when(repository.countByStatus(Status.INACTIVE)).thenReturn(2L);

        assertThat(service.countTotal()).isEqualTo(12L);
        assertThat(service.countActive()).isEqualTo(10L);
        assertThat(service.countInactive()).isEqualTo(2L);
    }
}