package com.vadim.document.service.model.mapper;

import com.vadim.document.service.model.dto.documenthistory.DocumentHistoryRequestDto;
import com.vadim.document.service.model.dto.documenthistory.DocumentHistoryResponseDto;
import com.vadim.document.service.model.entity.DocumentHistoryEntity;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface DocumentHistoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "document.id", source = "documentId")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    DocumentHistoryEntity toEntity(DocumentHistoryRequestDto dto);

    @Mapping(target = "documentId", source = "document.id")
    DocumentHistoryResponseDto toResponseDto(DocumentHistoryEntity entity);

    List<DocumentHistoryResponseDto> toResponseDtoList(List<DocumentHistoryEntity> entities);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "document", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    void updateEntity(@MappingTarget DocumentHistoryEntity entity, DocumentHistoryRequestDto dto);
}
