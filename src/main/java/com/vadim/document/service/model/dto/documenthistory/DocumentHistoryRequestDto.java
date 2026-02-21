package com.vadim.document.service.model.dto.documenthistory;

import com.vadim.document.service.model.enums.DocumentAction;
import com.vadim.document.service.model.enums.DocumentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentHistoryRequestDto {
    @NotNull
    private UUID documentId;
    @NotNull
    private DocumentAction action;
    @NotNull(message = "User is required")
    private String user;
    private String comment;
    private DocumentStatus fromStatus;
    private DocumentStatus toStatus;


}
