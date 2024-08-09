package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.animal.AnimalPreferenceDTO;
import com.example.petbuddybackend.entity.animal.AnimalPreference;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AnimalPreferenceMapper {

    AnimalPreferenceMapper INSTANCE = Mappers.getMapper(AnimalPreferenceMapper.class);

    AnimalPreferenceDTO mapToAnimalPreferenceDTO(AnimalPreference animalPreference);

}
