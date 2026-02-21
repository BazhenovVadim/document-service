package com.vadim.document.service.interfaces;

import com.vadim.document.service.config.exceptions.NotFoundException;
import com.vadim.document.service.config.exceptions.RegistryException;
import com.vadim.document.service.model.entity.DocumentEntity;

public interface DocumentProcessor {
    void process(DocumentEntity document, String comment, String user) throws RegistryException, NotFoundException;
}
