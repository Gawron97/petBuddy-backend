package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.care.CareDTO;
import com.example.petbuddybackend.dto.care.DetailedCareDTO;
import com.example.petbuddybackend.dto.care.DetailedCareWithHistoryDTO;
import com.example.petbuddybackend.dto.care.UpdateCareDTO;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(uses = {UserMapper.class, CareStatusesHistoryMapper.class})
public interface CareMapper {

    CareMapper INSTANCE = Mappers.getMapper(CareMapper.class);

    @Mapping(target = "selectedOptions", source = "animalAttributes", qualifiedByName = "mapSelectedOptions")
    @Mapping(target = "animalType", source = "animal.animalType")
    @Mapping(target = "caretakerEmail", source = "caretaker.email")
    @Mapping(target = "clientEmail", source = "client.email")
    @Mapping(target = "submittedAt", source = "submittedAt", qualifiedByName = "mapToZonedDateTime")
    CareDTO mapToCareDTO(Care care, @Context ZoneId zoneId);

    @Mapping(target = "selectedOptions", source = "care.animalAttributes", qualifiedByName = "mapSelectedOptions")
    @Mapping(target = "animalType", source = "care.animal.animalType")
    @Mapping(target = "caretaker", source = "care.caretaker.accountData")
    @Mapping(target = "client", source = "care.client.accountData")
    @Mapping(target = "submittedAt", source = "care.submittedAt", qualifiedByName = "mapToZonedDateTime")
    DetailedCareDTO mapToDetailedCareDTO(Care care, @Context ZoneId zoneId, Boolean canBeRated);

    @Mapping(target = "selectedOptions", source = "care.animalAttributes", qualifiedByName = "mapSelectedOptions")
    @Mapping(target = "animalType", source = "care.animal.animalType")
    @Mapping(target = "caretaker", source = "care.caretaker.accountData")
    @Mapping(target = "client", source = "care.client.accountData")
    @Mapping(target = "statusesHistory", source = "care.careStatusesHistory")
    @Mapping(target = "submittedAt", source = "care.submittedAt", qualifiedByName = "mapToZonedDateTime")
    DetailedCareWithHistoryDTO mapToDetailedCareWithHistoryDTO(Care care, @Context ZoneId zoneId, Boolean canBeRated);

    void updateCareFromDTO(UpdateCareDTO careDTO, @MappingTarget Care care);

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

    @Named("mapToCanBeRated")
    default Boolean mapToCanBeRated(Care care) {
        return CareStatus.CONFIRMED.equals(care.getClientStatus()) &&
               CareStatus.CONFIRMED.equals(care.getCaretakerStatus());
    }

}
