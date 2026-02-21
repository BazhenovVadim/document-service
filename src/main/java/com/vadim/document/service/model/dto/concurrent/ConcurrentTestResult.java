package com.vadim.document.service.model.dto.concurrent;

import com.vadim.document.service.model.enums.DocumentStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ConcurrentTestResult {
    private UUID documentId;
    private DocumentStatus finalStatus;
    private int successfulAttempts;
    private int conflictAttempts;
    private int errorAttempts;
    private int totalAttempts;
    private long executionTimeMs;
    private Instant testTime;
    private boolean registryEntryCreated;

    public String getSummary() {
        return String.format(
                "Document: %s, Final status: %s, Success: %d, Conflicts: %d, Errors: %d, Time: %d ms",
                documentId, finalStatus, successfulAttempts, conflictAttempts, errorAttempts, executionTimeMs
        );
    }
}