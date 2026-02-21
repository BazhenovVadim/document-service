package com.vadim.document.service;

import com.vadim.document.service.model.dto.ProcessDtoResult;
import com.vadim.document.service.model.dto.document.DocumentPostRequestDto;
import com.vadim.document.service.model.dto.document.DocumentResponseDto;
import com.vadim.document.service.model.entity.DocumentEntity;
import com.vadim.document.service.model.enums.DocumentStatus;
import com.vadim.document.service.model.enums.ProcessStatus;
import com.vadim.document.service.repository.ApprovalRegistryRepository;
import com.vadim.document.service.repository.DocumentHistoryRepository;
import com.vadim.document.service.repository.DocumentRepository;
import com.vadim.document.service.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@Testcontainers
class DocumentServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    @Autowired
    private DocumentService documentService;
    @Autowired
    private DocumentRepository documentRepository;
    @SpyBean
    private ApprovalRegistryRepository registryRepository;
    @Autowired
    private DocumentHistoryRepository documentHistoryRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void happyPath_shouldCreateAndSubmitAndApproveDocument() {
        DocumentPostRequestDto createDto = new DocumentPostRequestDto();
        createDto.setAuthor("Иван");
        createDto.setName("Тест");

        DocumentResponseDto created = documentService.createDocument(createDto);

        assertThat(created.getDocumentStatus()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(created.getAuthor()).isEqualTo("Иван");

        UUID docId = created.getId();

        List<ProcessDtoResult> submitResults = documentService.submitDocument(
                List.of(docId), "user1", "test submit");

        assertThat(submitResults).hasSize(1);
        assertThat(submitResults.get(0).getProcessStatus()).isEqualTo(ProcessStatus.SUCCESS);

        DocumentEntity afterSubmit = documentRepository.findById(docId).get();
        assertThat(afterSubmit.getDocumentStatus()).isEqualTo(DocumentStatus.SUBMITTED);

        List<ProcessDtoResult> approveResults = documentService.approveDocument(
                List.of(docId), "user2", "test approve");

        assertThat(approveResults).hasSize(1);
        assertThat(approveResults.get(0).getProcessStatus()).isEqualTo(ProcessStatus.SUCCESS);

        DocumentEntity afterApprove = documentRepository.findById(docId).get();
        assertThat(afterApprove.getDocumentStatus()).isEqualTo(DocumentStatus.APPROVED);
        assertThat(registryRepository.existsByDocumentId(docId)).isTrue();
        assertThat(documentHistoryRepository.findByDocumentIdOrderByTimestampDesc(docId)).hasSize(2);
    }

    @Test
    @Transactional
    @Rollback
    void batchSubmit_shouldReturnPartialResults() {
        DocumentEntity draft1 = createTestDocument("DRAFT1", DocumentStatus.DRAFT);
        DocumentEntity draft2 = createTestDocument("DRAFT2", DocumentStatus.DRAFT);
        DocumentEntity alreadySubmitted = createTestDocument("SUBMITTED", DocumentStatus.SUBMITTED);


        List<UUID> ids = List.of(draft1.getId(), draft2.getId(),
                alreadySubmitted.getId());

        List<ProcessDtoResult> results = documentService.submitDocument(ids, "user", "batch test");

        assertThat(results).hasSize(3);

        // 2 успеха
        assertThat(results.stream()
                .filter(r -> r.getProcessStatus() == ProcessStatus.SUCCESS))
                .hasSize(2);

        assertThat(results.stream()
                .filter(r -> r.getProcessStatus() == ProcessStatus.CONFLICT))
                .hasSize(1);
    }

    @Test
    void approve_shouldReturnRegistryErrorButKeepStatusChanged() {

        DocumentEntity document =
                createTestDocument("FOR_APPROVE", DocumentStatus.SUBMITTED);

        UUID docId = document.getId();
        doThrow(new RuntimeException("DB down"))
                .when(registryRepository)
                .save(any());

        List<ProcessDtoResult> results =
                documentService.approveDocument(List.of(docId), "user", "test");
        assertThat(results.get(0).getProcessStatus())
                .isEqualTo(ProcessStatus.REGISTRY_ERROR);
        DocumentEntity after = documentRepository.findById(docId).get();
        assertThat(after.getDocumentStatus())
                .isEqualTo(DocumentStatus.SUBMITTED);

        assertThat(registryRepository.existsByDocumentId(docId))
                .isFalse();
    }

    private DocumentEntity createTestDocument(String name, DocumentStatus status) {
        DocumentEntity entity = new DocumentEntity();
        entity.setPersonalNumber("TEST-" + UUID.randomUUID());
        entity.setAuthor("Test Author");
        entity.setName(name);
        entity.setDocumentStatus(status);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return documentRepository.save(entity);
    }
}