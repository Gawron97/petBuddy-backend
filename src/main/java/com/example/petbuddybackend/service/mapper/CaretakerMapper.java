package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.entity.user.Caretaker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CaretakerMapper {

    CaretakerMapper INSTANCE = Mappers.getMapper(CaretakerMapper.class);

    @Mapping(source = "accountData.name", target = "name")
    @Mapping(source = "accountData.surname", target = "surname")
    @Mapping(target = "address", source = "address")
    CaretakerDTO mapToCaretakerDTO(Caretaker caretaker);
}
