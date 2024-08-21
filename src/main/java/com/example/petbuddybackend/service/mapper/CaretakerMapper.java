package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.CreateCaretakerDTO;
import com.example.petbuddybackend.dto.user.UpdateCaretakerDTO;
import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.user.Caretaker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.util.StringUtils;

@Mapper(uses = {OfferMapper.class, AddressMapper.class})
public interface CaretakerMapper {

    CaretakerMapper INSTANCE = Mappers.getMapper(CaretakerMapper.class);

    @Mapping(target = "animals", source = "offers", qualifiedByName = "mapAnimalFromOffer")
    CaretakerDTO mapToCaretakerDTO(Caretaker caretaker);

    @Named("mapAnimalFromOffer")
    default String mapAnimalFromOffer(Offer offer) {
        return offer.getAnimal().getAnimalType();
    }

    Caretaker mapToCaretaker(CreateCaretakerDTO caretakerDTO);

    default void updateCaretakerFromDTO(UpdateCaretakerDTO caretakerDTO, @MappingTarget Caretaker caretaker) {
        if (StringUtils.hasText(caretakerDTO.phoneNumber())) {
            caretaker.setPhoneNumber(caretakerDTO.phoneNumber());
        }
        if (StringUtils.hasText(caretakerDTO.description())) {
            caretaker.setDescription(caretakerDTO.description());
        }
        if (caretakerDTO.address() != null) {
            if(caretaker.getAddress() == null) {
                caretaker.setAddress(new Address());
            }
            AddressMapper.INSTANCE.updateAddressFromDTO(caretakerDTO.address(), caretaker.getAddress());
        }
    }

}
