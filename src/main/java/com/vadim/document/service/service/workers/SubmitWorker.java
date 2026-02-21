package com.vadim.document.service.service;

import com.vadim.document.service.config.worker.WorkerProperties;
import com.vadim.document.service.interfaces.AbstractDocumentWorker;
import com.vadim.document.service.model.dto.ProcessDtoResult;
import com.vadim.document.service.model.entity.DocumentEntity;
import com.vadim.document.service.model.enums.DocumentStatus;
import com.vadim.document.service.repository.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class SubmitWorker extends AbstractDocumentWorker {

    public SubmitWorker(
            WorkerProperties properties,
            DocumentRepository documentRepository,
            DocumentService documentService) {
        super(properties, documentRepository, documentService);
    }

    @Override
    protected String getWorkerName() {
        return "SUBMIT-WORKER";
    }

    @Override
    protected long getIntervalMs() {
        return properties.getSubmitIntervalMs();
    }

    @Override
    protected List<DocumentEntity> findDocumentsToProcess(int batchSize) {
        // Находим DRAFT документы, сортируем по дате создания (старые первыми)
        return documentRepository.findTopByStatusOrderByCreatedAtAsc(
                DocumentStatus.DRAFT, batchSize
        );
    }

    @Override
    protected List<ProcessDtoResult> processDocuments(List<UUID> documentIds) {
        log.debug("Submitting {} documents", documentIds.size());
        return documentService.submitDocument(
                documentIds,
                "SYSTEM",
                "Auto-submitted by background worker"
        );
    }
}