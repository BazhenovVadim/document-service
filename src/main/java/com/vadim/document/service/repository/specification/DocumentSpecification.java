package com.vadim.document.service.repository.specification;

import com.vadim.document.service.model.entity.DocumentEntity;
import com.vadim.document.service.model.enums.DocumentStatus;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;
import java.time.Instant;

public class DocumentSpecification {

    // Фильтр по статусу
    public static Specification<DocumentEntity> hasStatus(DocumentStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("documentStatus"), status);
        };
    }

    // Фильтр по автору (частичное совпадение, регистронезависимое)
    public static Specification<DocumentEntity> authorContains(String author) {
        return (root, query, cb) -> {
            if (author == null || author.trim().isEmpty()) return cb.conjunction();
            return cb.like(cb.lower(root.get("author")),
                    "%" + author.toLowerCase() + "%");
        };
    }

    // Фильтр по дате создания (период)
    public static Specification<DocumentEntity> createdAtBetween(Instant from, Instant to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return cb.conjunction();

            Path<Instant> createdAt = root.get("createdAt");

            if (from != null && to != null) {
                return cb.between(createdAt, from, to);
            } else if (from != null) {
                return cb.greaterThanOrEqualTo(createdAt, from);
            } else if (to != null) {
                return cb.lessThanOrEqualTo(createdAt, to);
            }

            return cb.conjunction();
        };
    }

    // Фильтр по дате обновления (период)
    public static Specification<DocumentEntity> updatedAtBetween(Instant from, Instant to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return cb.conjunction();

            Path<Instant> updatedAt = root.get("updatedAt");

            if (from != null && to != null) {
                return cb.between(updatedAt, from, to);
            } else if (from != null) {
                return cb.greaterThanOrEqualTo(updatedAt, from);
            } else if (to != null) {
                return cb.lessThanOrEqualTo(updatedAt, to);
            }

            return cb.conjunction();
        };
    }

    // Комбинированный фильтр
    public static Specification<DocumentEntity> filterBy(
            DocumentStatus status,
            String author,
            Instant dateFrom,
            Instant dateTo,
            boolean searchByCreatedAt) {

        Specification<DocumentEntity> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and(hasStatus(status));
        }

        if (author != null && !author.trim().isEmpty()) {
            spec = spec.and(authorContains(author));
        }

        // Выбираем поле для поиска по дате
        if (searchByCreatedAt) {
            spec = spec.and(createdAtBetween(dateFrom, dateTo));
        } else {
            spec = spec.and(updatedAtBetween(dateFrom, dateTo));
        }

        return spec;
    }
}
