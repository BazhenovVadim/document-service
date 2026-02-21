package com.vadim.document.service.repository;

import com.vadim.document.service.model.entity.DocumentHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentHistoryRepository extends JpaRepository<DocumentHistoryEntity, UUID> {
    List<DocumentHistoryEntity> findByDocumentIdOrderByTimestampDesc(UUID docId);
}
