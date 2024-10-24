package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.animal.AnimalComplexInfoDTO;
import com.example.petbuddybackend.dto.animal.AnimalDTO;
import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface AnimalMapper {

    AnimalMapper INSTANCE = Mappers.getMapper(AnimalMapper.class);

    AnimalDTO mapToAnimalDTO(Animal animal);

    @Mapping(target = "animalAttributes", source = "animalAttributes", qualifiedByName = "mapToAnimalAttributes")
    @Mapping(target = "amenities", source = "animalAmenities", qualifiedByName = "mapToAmenities")
    AnimalComplexInfoDTO mapToAnimalComplexInfoDTO(Animal animal);

    @Named("mapToAnimalAttributes")
    default Map<String, List<String>> mapToAnimalAttributes(List<AnimalAttribute> animalAttributes) {
        return animalAttributes.stream()
                .collect(Collectors.groupingBy(
                        AnimalAttribute::getAttributeName,
                        Collectors.mapping(AnimalAttribute::getAttributeValue, Collectors.toList())
                ));
    }

    @Named("mapToAmenities")
    default List<String> mapToAmenities(List<AnimalAmenity> animalAmenities) {
        return animalAmenities.stream()
                .map(animalAmenity -> animalAmenity.getAmenity().getName())
                .collect(Collectors.toList());
    }

}
