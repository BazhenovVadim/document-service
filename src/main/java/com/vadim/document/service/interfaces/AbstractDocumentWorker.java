package com.vadim.document.service.interfaces;

import com.vadim.document.service.config.worker.WorkerProperties;
import com.vadim.document.service.model.dto.ProcessDtoResult;
import com.vadim.document.service.model.entity.DocumentEntity;
import com.vadim.document.service.model.enums.ProcessStatus;
import com.vadim.document.service.repository.DocumentRepository;
import com.vadim.document.service.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class AbstractDocumentWorker {

    protected final WorkerProperties properties;
    protected final DocumentRepository documentRepository;
    protected final DocumentService documentService;

    private final AtomicInteger processedCount = new AtomicInteger(0);
    private final AtomicInteger totalProcessed = new AtomicInteger(0);

    protected AbstractDocumentWorker(
            WorkerProperties properties,
            DocumentRepository documentRepository,
            DocumentService documentService) {
        this.properties = properties;
        this.documentRepository = documentRepository;
        this.documentService = documentService;
    }

    @Async
    public void execute() {
        if (!properties.isEnabled()) {
            log.info("{} is disabled", getWorkerName());
            return;
        }

        log.info("Starting {} with batch size: {}", getWorkerName(), properties.getBatchSize());

        while (!Thread.currentThread().isInterrupted()) {
            try {
                processBatch();
                Thread.sleep(getIntervalMs());
            } catch (InterruptedException e) {
                log.info("{} interrupted", getWorkerName());
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error in {}: {}", getWorkerName(), e.getMessage(), e);
                try {
                    Thread.sleep(10000); // пауза при ошибке
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.info("{} stopped. Total processed: {}", getWorkerName(), totalProcessed.get());
    }

    @Transactional
    public void processBatch() {
        long startTime = System.currentTimeMillis();
        List<DocumentEntity> documents = findDocumentsToProcess(properties.getBatchSize());
        if (documents.isEmpty()) {
            log.debug("No documents to process in {}", getWorkerName());
            return;
        }
        List<UUID> documentIds = documents.stream()
                .map(DocumentEntity::getId)
                .toList();
        log.info("{} found {} documents to process", getWorkerName(), documentIds.size());
        List<ProcessDtoResult> results = processDocuments(documentIds);
        long successCount = results.stream()
                .filter(r -> r.getProcessStatus() == ProcessStatus.SUCCESS)
                .count();

        long conflictCount = results.stream()
                .filter(r -> r.getProcessStatus() == ProcessStatus.CONFLICT)
                .count();

        long errorCount = results.stream()
                .filter(r -> r.getProcessStatus() == ProcessStatus.REGISTRY_ERROR)
                .count();

        processedCount.addAndGet((int) successCount);
        totalProcessed.addAndGet((int) successCount);

        long duration = System.currentTimeMillis() - startTime;

        log.info("{} processed batch: {} success, {} conflict, {} error, duration: {} ms",
                getWorkerName(), successCount, conflictCount, errorCount, duration);
        log.info("{} total processed so far: {}", getWorkerName(), totalProcessed.get());
    }

    protected abstract String getWorkerName();

    protected abstract long getIntervalMs();

    protected abstract List<DocumentEntity> findDocumentsToProcess(int batchSize);

    protected abstract List<ProcessDtoResult> processDocuments(List<UUID> documentIds);
}