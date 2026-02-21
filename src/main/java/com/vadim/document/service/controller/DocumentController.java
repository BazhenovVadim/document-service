package com.vadim.document.service.controller;

import com.vadim.document.service.model.dto.ProcessRequest;
import com.vadim.document.service.model.dto.document.DocumentFilterDto;
import com.vadim.document.service.model.dto.ProcessDtoResult;
import com.vadim.document.service.model.dto.document.DocumentPostRequestDto;
import com.vadim.document.service.model.dto.document.DocumentResponseDto;
import com.vadim.document.service.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/document")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    @PostMapping
    public DocumentResponseDto createDocument(@RequestBody DocumentPostRequestDto documentPostRequestDto) {
        return documentService.createDocument(documentPostRequestDto);
    }

    @GetMapping("/all-documents")
    public Page<DocumentResponseDto> getAllDocuments(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @RequestParam(defaultValue = "createdAt") String sortBy,
                                                     @RequestParam(defaultValue = "desc") String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return documentService.getAllDocuments(pageable);
    }

    @GetMapping("/{documentId}")
    public DocumentResponseDto getDocument(@PathVariable UUID documentId) {
        return documentService.getDocument(documentId);
    }

    @PostMapping("/submit")
    public List<ProcessDtoResult> submitDocuments(@RequestBody ProcessRequest processRequest) {
        return documentService.submitDocument(
                processRequest.getDocumentsId(),
                processRequest.getUser(),
                processRequest.getComment());
    }

    @PostMapping("/approve")
    public List<ProcessDtoResult> approveDocument(@RequestBody ProcessRequest processRequest){
        return documentService.approveDocument(processRequest.getDocumentsId(),
                processRequest.getUser(),
                processRequest.getComment());
    }

    @GetMapping("/search")
    public Page<DocumentResponseDto> searchDocs(@Valid DocumentFilterDto documentFilterDto) {
        return documentService.search(documentFilterDto);
    }
}
