package com.newwork.backend.mapper;

import com.newwork.backend.dto.UserSummaryDTO;
import com.newwork.backend.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserSummaryDTO toUserSummaryDTO(User user);
}