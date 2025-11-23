package com.newwork.backend.mapper;

import com.newwork.backend.dto.FeedbackDTO;
import com.newwork.backend.model.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

  // Maps Entity -> Full DTO
  FeedbackDTO toDto(Feedback feedback);

  // Maps DTO -> Entity (for updates)
  @Mapping(target = "author", ignore = true)
  Feedback toEntity(FeedbackDTO dto);
}