package com.vadim.document.service.model.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ProcessRequest {
    private List<UUID> documentsId;
    private String user;
    private String comment;
}