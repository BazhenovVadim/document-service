package com.vadim.document.service.utils;

import com.vadim.document.service.model.dto.document.DocumentPostRequestDto;
import com.vadim.document.service.model.dto.document.DocumentResponseDto;
import com.vadim.document.service.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentGenerator {

    private final DocumentService documentService;

    public void generateDocuments(int count, String author) {
        log.info("Starting generation of {} documents", count);

        long startTime = System.currentTimeMillis();
        AtomicInteger successCount = new AtomicInteger(0);
        List<DocumentPostRequestDto> batch = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            DocumentPostRequestDto dto = new DocumentPostRequestDto();
            dto.setAuthor(author);
            dto.setName("Generated Document " + i + " - " + UUID.randomUUID().toString().substring(0, 8));

            batch.add(dto);

            if (batch.size() == 100 || i == count) {
                try {
                    List<DocumentResponseDto> results = documentService.createDocuments(batch);
                    successCount.addAndGet(results.size());
                    log.info("Progress: {}/{} documents created", successCount.get(), count);
                } catch (Exception e) {
                    log.error("Failed to create batch: {}", e.getMessage());
                }
                batch.clear();
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Generation completed: {} documents created in {} ms", successCount.get(), duration);
        log.info("Average time per document: {} ms", duration / count);
    }
}