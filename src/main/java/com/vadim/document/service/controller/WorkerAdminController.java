package com.vadim.document.service.controller;

import com.vadim.document.service.config.worker.WorkerProperties;
import com.vadim.document.service.model.enums.DocumentStatus;
import com.vadim.document.service.repository.DocumentRepository;
import com.vadim.document.service.service.workers.ApproveWorker;
import com.vadim.document.service.service.workers.SubmitWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/workers")
@RequiredArgsConstructor
public class WorkerAdminController {

    private final SubmitWorker submitWorker;
    private final ApproveWorker approveWorker;
    private final DocumentRepository documentRepository;
    private final WorkerProperties properties;

    @GetMapping("/status")
    public Map<String, Object> getWorkersStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("batchSize", properties.getBatchSize());
        status.put("enabled", properties.isEnabled());
        status.put("submitIntervalMs", properties.getSubmitIntervalMs());
        status.put("approveIntervalMs", properties.getApproveIntervalMs());

        Map<String, Long> queueSizes = new HashMap<>();
        queueSizes.put("draft", documentRepository.countByDocumentStatus(DocumentStatus.DRAFT));
        queueSizes.put("submitted", documentRepository.countByDocumentStatus(DocumentStatus.SUBMITTED));

        status.put("queues", queueSizes);

        return status;
    }

    @PostMapping("/trigger/submit")
    public String triggerSubmitWorker() {
        submitWorker.processBatch();
        return "Submit worker triggered";
    }

    @PostMapping("/trigger/approve")
    public String triggerApproveWorker() {
        approveWorker.processBatch();
        return "Approve worker triggered";
    }
}
