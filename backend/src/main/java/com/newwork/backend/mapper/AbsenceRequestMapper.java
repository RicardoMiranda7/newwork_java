package com.newwork.backend.mapper;

import com.newwork.backend.dto.AbsenceRequestDTO;
import com.newwork.backend.model.AbsenceRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AbsenceRequestMapper {

  // Maps Entity -> Full DTO
  AbsenceRequestDTO toDto(AbsenceRequest absenceRequest);

  // Maps DTO -> Entity (for updates)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "employee", ignore = true)
  AbsenceRequest toEntity(AbsenceRequestDTO dto);
}