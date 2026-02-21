package com.vadim.document.service.service.workers;

import com.vadim.document.service.config.worker.WorkerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@EnableAsync
@RequiredArgsConstructor
public class WorkerScheduler {

    private final SubmitWorker submitWorker;
    private final ApproveWorker approveWorker;
    private final WorkerProperties properties;

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void startWorkers() {
        log.info("Starting background workers with batch size: {}", properties.getBatchSize());

        CompletableFuture.allOf(
                CompletableFuture.runAsync(submitWorker::execute),
                CompletableFuture.runAsync(approveWorker::execute)
        ).join();
    }
}