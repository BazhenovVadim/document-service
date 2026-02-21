package com.vadim.document.service.repository;

import com.vadim.document.service.model.entity.ApprovalRegistryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApprovalRegistryRepository extends JpaRepository<ApprovalRegistryEntity, UUID> {
    boolean existsByDocumentId(UUID id);
}
