package org.inno.service.mapper;

import org.inno.dao.model.CardModel;
import org.inno.service.dto.CardDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "user.id")
    CardDto modelToDto(CardModel cardModel);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "user.id", source = "userId")
    CardModel dtoToModel(CardDto cardDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "user.id", source = "userId")
    void updateFromDto(CardDto cardDto, @MappingTarget CardModel cardModel);

}
