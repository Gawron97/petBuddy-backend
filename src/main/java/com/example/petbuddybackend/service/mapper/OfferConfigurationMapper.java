package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.offer.OfferConfigurationDTO;
import com.example.petbuddybackend.entity.offer.OfferConfiguration;
import com.example.petbuddybackend.entity.offer.OfferOption;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Mapper
public interface OfferConfigurationMapper {

    OfferConfigurationMapper INSTANCE = Mappers.getMapper(OfferConfigurationMapper.class);

    @Mapping(target = "selectedOptions", source = "offerOptions", qualifiedByName = "mapSelectedOptions")
    OfferConfigurationDTO mapToOfferConfigurationDTO(OfferConfiguration offerConfiguration);

    @Named("mapSelectedOptions")
    default Map<String, List<String>> mapSelectedOptions(List<OfferOption> offerOptions) {
        return offerOptions.stream()
                .collect(Collectors.groupingBy(
                        offerOption -> offerOption.getAnimalAttribute().getAttributeName(),
                        Collectors.mapping(
                                offerOption -> offerOption.getAnimalAttribute().getAttributeValue(),
                                Collectors.toList()
                        )
                ));
    }

}
