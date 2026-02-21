package com.vadim.document.service.service;

import com.vadim.document.service.model.dto.documenthistory.DocumentHistoryRequestDto;
import com.vadim.document.service.model.mapper.DocumentHistoryMapper;
import com.vadim.document.service.repository.DocumentHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentHistoryService {
    private final DocumentHistoryRepository documentHistoryRepository;
    private final DocumentHistoryMapper documentHistoryMapper;

    public void saveAllHistories(List<DocumentHistoryRequestDto> historyRequest) {
        documentHistoryRepository.saveAll(
                historyRequest
                        .stream()
                        .map(documentHistoryMapper::toEntity)
                        .toList());
    }
}

