package com.vadim.document.service.model.dto;

import com.vadim.document.service.model.enums.DocumentStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.Instant;

@Data
public class DocumentFilterDto {

    private DocumentStatus status;

    private String author;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant dateTo;

    private boolean searchByCreatedAt = true;

    private int page = 0;
    private int size = 20;

    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}