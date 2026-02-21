package com.vadim.document.service.model.dto;

import com.vadim.document.service.model.enums.DocumentStatus;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentPostRequestDto {
    private UUID id;
    private String author;
    private String name;
}
