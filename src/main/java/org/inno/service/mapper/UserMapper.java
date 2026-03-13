package org.inno.service.mapper;

import org.inno.dao.model.UserModel;
import org.inno.service.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = CardMapper.class)
public interface UserMapper {


    @Mapping(target = "cards", source = "cardDtos")
    UserModel dtoToModel(UserDto userDto);

    @Mapping(target = "cardDtos", source = "cards")
    UserDto modelToDto(UserModel userModel);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "cards", ignore = true)
    void updateFromDto(UserDto userDto, @MappingTarget UserModel userModel);
}
