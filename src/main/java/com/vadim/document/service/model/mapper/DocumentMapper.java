package com.vadim.document.service.model.mapper;

import com.vadim.document.service.model.dto.document.DocumentPostRequestDto;
import com.vadim.document.service.model.dto.document.DocumentResponseDto;
import com.vadim.document.service.model.entity.DocumentEntity;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface DocumentMapper {

    @Mapping(target = "personalNumber", ignore = true)
    @Mapping(target = "documentStatus", constant = "DRAFT")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    DocumentEntity toEntity(DocumentPostRequestDto dto);

    DocumentResponseDto toResponseDto(DocumentEntity entity);

    DocumentEntity entityToResponseDto(DocumentResponseDto documentResponseDto);

    List<DocumentResponseDto> toResponseDtoList(List<DocumentEntity> entities);
}