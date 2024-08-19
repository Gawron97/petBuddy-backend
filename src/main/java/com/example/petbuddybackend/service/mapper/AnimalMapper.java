package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.animal.AnimalDTO;
import com.example.petbuddybackend.entity.animal.Animal;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AnimalMapper {

    AnimalMapper INSTANCE = Mappers.getMapper(AnimalMapper.class);

    AnimalDTO mapToAnimalDTO(Animal animal);

}
