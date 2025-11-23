package com.newwork.backend.mapper;

import com.newwork.backend.dto.AbsenceDTO;
import com.newwork.backend.model.AbsenceRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AbsenceMapper {

  // Maps Entity -> Full DTO
  @Mapping(target = "employee", source = "employee.email")
  AbsenceDTO toDto(AbsenceRequest absenceRequest);

  // Maps DTO -> Entity (for updates)
  @Mapping(target = "employee", ignore = true)
  @Mapping(
      target = "status",
      defaultExpression = "java(com.newwork.backend.model.AbsenceStatus.PENDING)")
  AbsenceRequest toEntity(AbsenceDTO dto);
}