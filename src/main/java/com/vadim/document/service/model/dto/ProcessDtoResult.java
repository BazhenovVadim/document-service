package com.vadim.document.service.model.dto;

import com.vadim.document.service.model.enums.DocumentAction;
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
public class ProcessDtoResult {
    private UUID documentId;
    private String message;
    private ProcessStatus processStatus;

    public static ProcessDtoResult success(UUID documentId, DocumentAction documentAction) {
        return ProcessDtoResult.builder()
                .documentId(documentId)
                .message("Successfully " + documentAction)
                .processStatus(ProcessStatus.SUCCESS)
                .build();
    }

    public static ProcessDtoResult notFound(UUID documentId) {
        return ProcessDtoResult.builder()
                .documentId(documentId)
                .message("Document not found")
                .processStatus(ProcessStatus.NOT_FOUND)
                .build();
    }

    public static ProcessDtoResult registryError(UUID documentId) {
        return ProcessDtoResult.builder()
                .documentId(documentId)
                .message("Error while try approve document")
                .processStatus(ProcessStatus.REGISTRY_ERROR)
                .build();
    }

    public static ProcessDtoResult conflict(UUID documentId, String messageError) {
        return ProcessDtoResult.builder()
                .documentId(documentId)
                .message("Conflict " + messageError)
                .processStatus(ProcessStatus.CONFLICT)
                .build();
    }
}
