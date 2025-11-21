package com.newwork.backend.mapper;

import com.newwork.backend.dto.ProfileCoWorkerDTO;
import com.newwork.backend.dto.ProfileDTO;
import com.newwork.backend.model.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// componentModel = "spring" allows us to inject this mapper using @Autowired
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ProfileMapper {

  // Maps Entity -> Full DTO
  ProfileDTO toDto(Profile profile);

  // Maps Entity -> Restricted DTO
  // MapStruct automatically ignores fields in Entity that don't exist in DTO (like salary)
  ProfileCoWorkerDTO toCoWorkerDto(Profile profile);

  // Maps DTO -> Entity (for updates)
  @Mapping(target = "id", ignore = true) // Don't overwrite ID on create
  @Mapping(target = "user", ignore = true) // User is usually handled separately
  @Mapping(target = "manager", ignore = true)
  Profile toEntity(ProfileDTO dto);
}