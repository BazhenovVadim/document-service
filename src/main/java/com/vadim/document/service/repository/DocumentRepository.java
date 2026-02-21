package com.vadim.document.service.repository;

import com.vadim.document.service.model.entity.DocumentEntity;
import com.vadim.document.service.model.enums.DocumentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID>,
        JpaSpecificationExecutor<DocumentEntity> {


    // Для воркеров: найти N документов по статусу (старые первыми)
    @Query("SELECT d FROM DocumentEntity d " +
            "WHERE d.documentStatus = :status " +
            "ORDER BY d.createdAt ASC")
    List<DocumentEntity> findTopByStatusOrderByCreatedAtAsc(
            @Param("status") DocumentStatus status,
            Pageable pageable
    );

    default List<DocumentEntity> findTopByStatusOrderByCreatedAtAsc(
            DocumentStatus status, int limit) {
        return findTopByStatusOrderByCreatedAtAsc(status, Pageable.ofSize(limit));
    }

    // По updatedAt для APPROVE worker
    @Query("SELECT d FROM DocumentEntity d " +
            "WHERE d.documentStatus = :status " +
            "ORDER BY d.updatedAt ASC")
    List<DocumentEntity> findTopByStatusOrderByUpdatedAtAsc(
            @Param("status") DocumentStatus status,
            Pageable pageable
    );

    default List<DocumentEntity> findTopByStatusOrderByUpdatedAtAsc(
            DocumentStatus status, int limit) {
        return findTopByStatusOrderByUpdatedAtAsc(status, Pageable.ofSize(limit));
    }

    long countByDocumentStatus(DocumentStatus status);

}
