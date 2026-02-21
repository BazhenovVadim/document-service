package com.vadim.document.service.controller;

import com.vadim.document.service.model.dto.concurrent.ConcurrentTestResult;
import com.vadim.document.service.service.ConcurrentTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class ConcurrentTestController {

    private final ConcurrentTestService concurrentTestService;

    @PostMapping("/concurrent-approve/{documentId}")
    public ConcurrentTestResult testConcurrentApprove(
            @PathVariable UUID documentId,
            @RequestParam(defaultValue = "5") int threads,
            @RequestParam(defaultValue = "10") int attempts,
            @RequestParam(defaultValue = "test-user") String user) {

        return concurrentTestService.testConcurrentApprove(documentId, threads, attempts, user);
    }
}