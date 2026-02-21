package com.vadim.document.service.model.dto.documenthistory;

import com.vadim.document.service.model.enums.DocumentAction;
import com.vadim.document.service.model.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentHistoryResponseDto {
    private UUID id;
    private UUID documentId;
    private DocumentAction action;
    private String user;
    private String comment;
    private Instant timestamp;
    private DocumentStatus fromStatus;
    private DocumentStatus toStatus;
}