package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.CaretakerComplexDTO;
import com.example.petbuddybackend.dto.user.CaretakerComplexPublicDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.ModifyCaretakerDTO;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;


@Mapper(uses = {OfferMapper.class, AddressMapper.class, UserMapper.class})
public interface CaretakerMapper {

    CaretakerMapper INSTANCE = Mappers.getMapper(CaretakerMapper.class);

    @Mapping(target = "animals", source = "caretaker.offers", qualifiedByName = "mapAnimalFromOffer")
    CaretakerComplexDTO mapToCaretakerComplexDTO(Caretaker caretaker);

    @Mapping(target = "accountData", source = "accountData")
    Caretaker mapToCaretaker(ModifyCaretakerDTO caretakerDTO, AppUser accountData);

    @Mapping(target = "animals", source = "caretaker.offers", qualifiedByName = "mapAnimalFromOffer")
    CaretakerDTO mapToCaretakerDTO(Caretaker caretaker);

    @Mapping(target = "animals", source = "caretaker.offers", qualifiedByName = "mapAnimalFromOffer")
    CaretakerComplexPublicDTO mapToCaretakerComplexPublicDTO(Caretaker caretaker);

    void updateCaretakerFromDTO(ModifyCaretakerDTO caretakerDTO, @MappingTarget Caretaker caretaker);

    @Named("mapAnimalFromOffer")
    default String mapAnimalFromOffer(Offer offer) {
        return offer.getAnimal().getAnimalType();
    }

}
