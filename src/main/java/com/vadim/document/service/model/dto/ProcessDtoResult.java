package com.vadim.document.service.model.dto;

import com.vadim.document.service.model.enums.ProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessDto {
    private UUID documentId;
    private String message;
    private ProcessStatus processStatus;

    public static ProcessDto success(UUID documentId) {
        return ProcessDto.builder()
                .documentId(documentId)
                .message("Successfully submitted")
                .processStatus(ProcessStatus.SUCCESS)
                .build();
    }

    public static ProcessDto notFound(UUID documentId) {
        return ProcessDto.builder()
                .documentId(documentId)
                .message("Document not found")
                .processStatus(ProcessStatus.NOT_FOUND)
                .build();
    }

    public static ProcessDto conflict(UUID documentId) {
        return ProcessDto.builder()
                .documentId(documentId)
                .message("Successfully submitted")
                .processStatus(ProcessStatus.CONFLICT)
                .build();
    }
}
