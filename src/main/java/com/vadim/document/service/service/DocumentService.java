package com.vadim.document.service.service;

import com.vadim.document.service.config.exceptions.ConflictException;
import com.vadim.document.service.config.exceptions.NotFoundException;
import com.vadim.document.service.config.exceptions.RegistryException;
import com.vadim.document.service.interfaces.DocumentProcessor;
import com.vadim.document.service.model.dto.ProcessDtoResult;
import com.vadim.document.service.model.dto.document.DocumentFilterDto;
import com.vadim.document.service.model.dto.document.DocumentPostRequestDto;
import com.vadim.document.service.model.dto.document.DocumentResponseDto;
import com.vadim.document.service.model.dto.documenthistory.DocumentHistoryRequestDto;
import com.vadim.document.service.model.entity.ApprovalRegistryEntity;
import com.vadim.document.service.model.entity.DocumentEntity;
import com.vadim.document.service.model.enums.DocumentAction;
import com.vadim.document.service.model.enums.DocumentStatus;
import com.vadim.document.service.model.mapper.DocumentMapper;
import com.vadim.document.service.repository.ApprovalRegistryRepository;
import com.vadim.document.service.repository.DocumentRepository;
import com.vadim.document.service.repository.specification.DocumentSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final DocumentHistoryService documentHistoryService;
    private final ApprovalRegistryRepository approvalRegistryRepository;

    public DocumentEntity findById(UUID documentId) throws NotFoundException {
        return documentRepository.findById(documentId).orElseThrow(
                () -> new NotFoundException(String.format("Document with id=%s not found", documentId)));
    }

    public DocumentResponseDto getDocument(UUID documentId) {
        DocumentResponseDto documentResponseDto;
        try {
            documentResponseDto = documentMapper.toResponseDto(findById(documentId));
        } catch (NotFoundException e) {
            log.error("Document with id={} not found", documentId);
            return null;
        }
        return documentResponseDto;
    }

    @Transactional(readOnly = true)
    public Page<DocumentResponseDto> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable).map(documentMapper::toResponseDto);
    }

    @Transactional
    public List<DocumentResponseDto> createDocuments(List<DocumentPostRequestDto> documentPostRequestDtos) {
        return documentMapper.toResponseDtoList(
                documentPostRequestDtos
                        .stream()
                        .map(documentMapper::toEntity)
                        .toList());
    }


    @Transactional
    public DocumentResponseDto createDocument(DocumentPostRequestDto documentPostRequestDto) {
        DocumentEntity document = documentMapper.toEntity(documentPostRequestDto);
        document.setPersonalNumber(UUID.randomUUID().toString());
        DocumentEntity saved = documentRepository.save(document);

        return documentMapper.toResponseDto(saved);
    }

    public List<ProcessDtoResult> submitDocument(List<UUID> documentIds, String user, String comment) {
        return processDocument(documentIds, user, comment, DocumentStatus.SUBMITTED,
                DocumentAction.SUBMIT, DocumentStatus.DRAFT, null);
    }

    public List<ProcessDtoResult> approveDocument(List<UUID> documentIds, String user, String comment) {
        return processDocument(documentIds, user, comment, DocumentStatus.APPROVED, DocumentAction.APPROVE,
                DocumentStatus.SUBMITTED, this::createRegistryEntry);
    }

    @Transactional
    public List<ProcessDtoResult> processDocument(List<UUID> documentIds, String user, String comment,
                                                  DocumentStatus documentStatusTo, DocumentAction documentAction,
                                                  DocumentStatus documentStatusFrom, DocumentProcessor documentProcessor) {
        List<DocumentEntity> documents = documentRepository.findAllById(documentIds);
        List<DocumentHistoryRequestDto> histories = new ArrayList<>();
//        List<DocumentEntity> submittedDocuments = new ArrayList<>();
        List<ProcessDtoResult> results = new ArrayList<>();
        for (DocumentEntity document : documents) {
            if (document.getDocumentStatus() != documentStatusFrom) {
                results.add(ProcessDtoResult.conflict(document.getId(), "On check status"));
                continue;
            }
            try {
                if (documentProcessor != null) {
                    documentProcessor.process(document, comment, user);
                }
                document.setDocumentStatus(documentStatusTo);
                document.setUpdatedAt(Instant.now());
                histories.add(buildHistory(document.getId(), documentAction, user, comment, documentStatusFrom, documentStatusTo));
                results.add(ProcessDtoResult.success(document.getId(), documentAction));
            } catch (RegistryException e) {
                results.add(ProcessDtoResult.registryError(document.getId()));
            } catch (ConflictException | NotFoundException e) {
                results.add(ProcessDtoResult.conflict(document.getId(), e.getMessage()));
            }

        }
        documentHistoryService.saveAllHistories(histories);

        return results;
    }

    public Page<DocumentResponseDto> search(DocumentFilterDto filter) {
        var spec = DocumentSpecification.filterBy(
                filter.getStatus(),
                filter.getAuthor(),
                filter.getDateFrom(),
                filter.getDateTo(),
                filter.isSearchByCreatedAt()
        );
        Sort sort = Sort.by(
                filter.getSortDirection().equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                filter.getSortBy()
        );

        Pageable pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                sort
        );

        Page<DocumentEntity> entities = documentRepository.findAll(spec, pageable);

        return entities.map(documentMapper::toResponseDto);
    }

    private DocumentHistoryRequestDto buildHistory(UUID documentId, DocumentAction action, String user,
                                                   String comment, DocumentStatus fromStatus, DocumentStatus toStatus) {
        return DocumentHistoryRequestDto.builder()
                .documentId(documentId)
                .action(action)
                .user(user)
                .comment(comment)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .build();
    }


    private void createRegistryEntry(DocumentEntity document, String user, String comment) throws RegistryException, NotFoundException {
        if (approvalRegistryRepository.existsByDocumentId(document.getId())) {
            throw new ConflictException("Document already approved");
        }
        try {
            log.info("managdoc id = {}", document.getId());
            approvalRegistryRepository.save(ApprovalRegistryEntity.builder()
                    .document(document)
                    .approvedBy(user)
                    .approvedAt(Instant.now())
                    .registrationNumber(generateRegNumber())
                    .comment(comment)
                    .build());
        } catch (Exception e) {
            throw new RegistryException("Failed to create registry entry");
        }
    }

    private String generateRegNumber() {
        return "REG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
