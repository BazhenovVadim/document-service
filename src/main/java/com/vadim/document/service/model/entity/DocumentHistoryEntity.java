package com.vadim.document.service.model.entity;


import com.vadim.document.service.model.enums.DocumentAction;
import com.vadim.document.service.model.enums.DocumentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentEntity document;

    @Column(name = "action", nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentAction action;

    @Column(name = "user_name", nullable = false)
    private String user;

    @Column(name = "comment")
    private String comment;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "from_status")
    @Enumerated(EnumType.STRING)
    private DocumentStatus fromStatus;

    @Column(name = "to_status")
    @Enumerated(EnumType.STRING)
    private DocumentStatus toStatus;
}
