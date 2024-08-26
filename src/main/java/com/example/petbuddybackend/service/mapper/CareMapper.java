package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.care.CareDTO;
import com.example.petbuddybackend.dto.care.UpdateCareDTO;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.care.Care;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface CareMapper {

    CareMapper INSTANCE = Mappers.getMapper(CareMapper.class);

    @Mapping(target = "selectedOptions", source = "animalAttributes", qualifiedByName = "mapSelectedOptions")
    @Mapping(target = "animalType", source = "animal.animalType")
    @Mapping(target = "caretakerEmail", source = "caretaker.email")
    @Mapping(target = "clientEmail", source = "client.email")
    CareDTO mapToCareDTO(Care care);

    @Named("mapSelectedOptions")
    default Map<String, List<String>> mapSelectedOptions(Set<AnimalAttribute> animalAttributes) {
        return animalAttributes.stream()
                .collect(Collectors.groupingBy(
                        AnimalAttribute::getAttributeName,
                        Collectors.mapping(
                                AnimalAttribute::getAttributeValue,
                                Collectors.toList()
                        )
                ));
    }

    void updateCareFromDTO(UpdateCareDTO careDTO, @MappingTarget Care care);

}
