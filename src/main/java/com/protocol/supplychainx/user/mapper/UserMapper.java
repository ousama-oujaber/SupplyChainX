package com.protocol.supplychainx.user.mapper;

import com.protocol.supplychainx.user.dto.UserDTO;
import com.protocol.supplychainx.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    
    UserDTO toDTO(User user);
    
    User toEntity(UserDTO userDTO);
    
    void updateEntityFromDTO(UserDTO userDTO, @MappingTarget User user);
}
