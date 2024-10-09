package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.CaretakerComplexInfoDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.CreateCaretakerDTO;
import com.example.petbuddybackend.dto.user.ModifyCaretakerDTO;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Set;


@Mapper(uses = {OfferMapper.class, AddressMapper.class, UserMapper.class})
public interface CaretakerMapper {

    CaretakerMapper INSTANCE = Mappers.getMapper(CaretakerMapper.class);

    @Mapping(target = "animals", source = "caretaker.offers", qualifiedByName = "mapAnimalFromOffer")
    CaretakerComplexInfoDTO mapToCaretakerComplexInfoDTO(Caretaker caretaker);

    @Mapping(target = "accountData", source = "accountData")
    Caretaker mapToCaretaker(ModifyCaretakerDTO caretakerDTO, AppUser accountData, Set<PhotoLink> caretakerPhotos);

    // TODO: add test
    @Mapping(target = "accountData", source = "accountData")
    Caretaker mapToCaretaker(CreateCaretakerDTO caretakerDTO, AppUser accountData, Set<PhotoLink> caretakerPhotos);

    @Mapping(target = "animals", source = "caretaker.offers", qualifiedByName = "mapAnimalFromOffer")
    CaretakerDTO mapToCaretakerDTO(Caretaker caretaker);

    void updateCaretakerFromDTO(@MappingTarget Caretaker caretaker, ModifyCaretakerDTO caretakerDTO, Set<PhotoLink> offerPhotos);

    @Named("mapAnimalFromOffer")
    default String mapAnimalFromOffer(Offer offer) {
        return offer.getAnimal().getAnimalType();
    }

}
