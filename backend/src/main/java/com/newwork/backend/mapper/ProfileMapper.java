package com.newwork.backend.mapper;

import com.newwork.backend.dto.ProfileCoWorkerDTO;
import com.newwork.backend.dto.ProfileDTO;
import com.newwork.backend.model.UserProfile;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

// componentModel = "spring" allows to inject this mapper using @Autowired
@Mapper(componentModel = "spring")
public interface ProfileMapper {

  // Maps Entity -> Flattened DTO
  @Mapping(source = "user.email", target = "user")
  @Mapping(source = "manager.email", target = "manager")
  ProfileDTO toDto(UserProfile profile);

  // Maps Entity -> Restricted DTO
  // MapStruct automatically ignores fields in Entity that don't exist in DTO (like salary)
  ProfileCoWorkerDTO toCoWorkerDto(UserProfile profile);

  // Maps DTO -> Entity (for updates)
  @Mapping(target = "id", ignore = true) // Don't overwrite ID on create
  @Mapping(target = "user", ignore = true) // User is usually handled separately
  @Mapping(target = "manager", ignore = true)
  UserProfile toEntity(ProfileDTO dto);

  // Updates existing Entity from DTO
  @BeanMapping(nullValuePropertyMappingStrategy =
      NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true) // Don't overwrite ID
  @Mapping(target = "user", ignore = true) // Handle user separately
  @Mapping(target = "manager", ignore = true)
  // Handle manager separately
  void updateEntityFromDto(ProfileDTO dto, @MappingTarget UserProfile entity);

}