package com.vadim.document.service.model.dto.document;

import com.vadim.document.service.model.dto.documenthistory.DocumentHistoryResponseDto;
import com.vadim.document.service.model.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponseDto {
    private UUID id;

    private String personalNumber;

    private String author;

    private String name;

    private DocumentStatus documentStatus;

    private Instant createdAt;

    private Instant updatedAt;

    private List<DocumentHistoryResponseDto> historyEntities;
}
