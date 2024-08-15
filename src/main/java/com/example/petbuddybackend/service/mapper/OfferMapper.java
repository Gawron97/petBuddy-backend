package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.offer.OfferDTO;
import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.offer.Offer;
import org.keycloak.common.util.CollectionUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

@Mapper(uses = {AnimalMapper.class, OfferConfigurationMapper.class})
public interface OfferMapper {

    OfferMapper INSTANCE = Mappers.getMapper(OfferMapper.class);

    @Mapping(target = "animalAmenities", source = "animalAmenities", qualifiedByName = "mapAnimalAmenitiesToListOfString")
    OfferDTO mapToOfferDTO(Offer offer);

    @Named("mapAnimalAmenitiesToListOfString")
    default List<String> mapAnimalAmenitiesToListOfString(List<AnimalAmenity> animalAmenities) {
        if(CollectionUtil.isNotEmpty(animalAmenities)) {
            return animalAmenities.stream().map(animalAmenity -> animalAmenity.getAmenity().getAmenity()).toList();
        }
        return Collections.emptyList();
    }

}
