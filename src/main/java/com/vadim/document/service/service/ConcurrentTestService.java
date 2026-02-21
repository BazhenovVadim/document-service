package com.vadim.document.service.service;

import com.vadim.document.service.config.exceptions.ConflictException;
import com.vadim.document.service.model.dto.concurrent.ConcurrentTestResult;
import com.vadim.document.service.model.entity.DocumentEntity;
import com.vadim.document.service.model.enums.DocumentStatus;
import com.vadim.document.service.model.mapper.DocumentMapper;
import com.vadim.document.service.repository.ApprovalRegistryRepository;
import com.vadim.document.service.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConcurrentTestService {

    private final DocumentService documentService;
    private final DocumentRepository documentRepository;
    private final ApprovalRegistryRepository registryRepository;
    private final DocumentMapper documentMapper;

    public ConcurrentTestResult testConcurrentApprove(
            UUID documentId,
            int threads,
            int attempts,
            String user) {

        log.info("Starting concurrent test for document: {} with {} threads and {} attempts",
                documentId, threads, attempts);

        Instant start = Instant.now();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<AttemptResult>> futures = new ArrayList<>();

        for (int i = 0; i < attempts; i++) {
            int attemptNumber = i + 1;
            futures.add(executor.submit(() ->
                    tryApprove(documentId, user, attemptNumber)
            ));
        }
        int success = 0;
        int conflicts = 0;
        int errors = 0;

        for (Future<AttemptResult> future : futures) {
            try {
                AttemptResult result = future.get(10, TimeUnit.SECONDS);
                switch (result.getStatus()) {
                    case SUCCESS -> success++;
                    case CONFLICT -> conflicts++;
                    case ERROR -> errors++;
                }
            } catch (TimeoutException e) {
                log.error("Attempt timed out");
                errors++;
            } catch (Exception e) {
                log.error("Attempt failed: {}", e.getMessage());
                errors++;
            }
        }

        executor.shutdown();

        DocumentEntity finalDocument = documentMapper.entityToResponseDto(documentService.getDocument(documentId));
        boolean registryCreated = registryRepository.existsByDocumentId(documentId);
        long executionTime = java.time.Duration.between(start, Instant.now()).toMillis();

        ConcurrentTestResult result = ConcurrentTestResult.builder()
                .documentId(documentId)
                .finalStatus(finalDocument.getDocumentStatus())
                .successfulAttempts(success)
                .conflictAttempts(conflicts)
                .errorAttempts(errors)
                .totalAttempts(attempts)
                .executionTimeMs(executionTime)
                .testTime(Instant.now())
                .registryEntryCreated(registryCreated)
                .build();

        log.info("Test completed: {}", result.getSummary());
        validateResult(result);
        return result;
    }

    private AttemptResult tryApprove(UUID documentId, String user, int attemptNumber) {
        try {
            log.debug("Attempt {} starting", attemptNumber);
            documentService.approveDocument(List.of(documentId), user, "Concurrent test attempt " + attemptNumber);
            log.debug("Attempt {} SUCCESS", attemptNumber);
            return AttemptResult.success();
        } catch (ConflictException e) {
            log.debug("Attempt {} CONFLICT: {}", attemptNumber, e.getMessage());
            return AttemptResult.conflict(e.getMessage());
        } catch (Exception e) {
            log.debug("Attempt {} ERROR: {}", attemptNumber, e.getMessage());
            return AttemptResult.error(e.getMessage());
        }
    }

    private void validateResult(ConcurrentTestResult result) {
        if (result.getFinalStatus() == DocumentStatus.APPROVED) {
            if (result.getSuccessfulAttempts() != 1) {
                log.warn("Expected 1 successful attempt, but got: {}", result.getSuccessfulAttempts());
            }
            if (!result.isRegistryEntryCreated()) {
                log.warn("Document is APPROVED but no registry entry found!");
            }
        } else {
            if (result.getSuccessfulAttempts() > 0) {
                log.warn("Document not APPROVED but have successful attempts: {}", result.getSuccessfulAttempts());
            }
        }
    }

    private enum AttemptStatus {
        SUCCESS, CONFLICT, ERROR
    }

    @Value
    private static class AttemptResult {
        AttemptStatus status;
        String message;

        static AttemptResult success() {
            return new AttemptResult(AttemptStatus.SUCCESS, "Success");
        }

        static AttemptResult conflict(String message) {
            return new AttemptResult(AttemptStatus.CONFLICT, message);
        }

        static AttemptResult error(String message) {
            return new AttemptResult(AttemptStatus.ERROR, message);
        }
    }
}