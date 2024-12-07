package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.CaretakerComplexDTO;
import com.example.petbuddybackend.dto.user.CaretakerComplexPublicDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.ModifyCaretakerDTO;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;


@Mapper(uses = {OfferMapper.class, AddressMapper.class, UserMapper.class})
public interface CaretakerMapper {

    CaretakerMapper INSTANCE = Mappers.getMapper(CaretakerMapper.class);

    @Mapping(target = "animals", source = "caretaker.offers", qualifiedByName = "mapAnimalFromOffer")
    CaretakerComplexDTO mapToCaretakerComplexDTO(Caretaker caretaker);

    @Mapping(target = "offerPhotos", source = "offerPhotos")
    @Mapping(target = "accountData", source = "accountData")
    @Mapping(target = "email", source = "accountData.email")
    Caretaker mapToCaretaker(ModifyCaretakerDTO caretakerDTO, AppUser accountData, List<PhotoLink> offerPhotos);

    @Mapping(target = "animals", source = "caretaker.offers", qualifiedByName = "mapAnimalFromOffer")
    CaretakerDTO mapToCaretakerDTO(Caretaker caretaker);

    @Mapping(target = "animals", source = "caretaker.offers", qualifiedByName = "mapAnimalFromOffer")
    CaretakerComplexPublicDTO mapToCaretakerComplexPublicDTO(Caretaker caretaker, Boolean blocked);

    void updateCaretakerFromDTO(@MappingTarget Caretaker caretaker, ModifyCaretakerDTO caretakerDTO);

    @Named("mapAnimalFromOffer")
    default String mapAnimalFromOffer(Offer offer) {
        return offer.getAnimal().getAnimalType();
    }

    @IterableMapping(qualifiedByName = "mapOfferPhotosDirectly")
    default List<PhotoLink> mapOfferPhotos(List<PhotoLink> offerPhotos) {
        return offerPhotos;
    }
}
