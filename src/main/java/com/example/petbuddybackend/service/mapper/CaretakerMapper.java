package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.CaretakerComplexInfoDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.ModifyCaretakerDTO;
import com.example.petbuddybackend.dto.user.UpdateCaretakerDTO;
import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.util.StringUtils;


@Mapper(uses = {OfferMapper.class, AddressMapper.class, UserMapper.class})
public interface CaretakerMapper {

    CaretakerMapper INSTANCE = Mappers.getMapper(CaretakerMapper.class);

    @Mapping(target = "animals", source = "caretaker.offers", qualifiedByName = "mapAnimalFromOffer")
    CaretakerComplexInfoDTO mapToCaretakerComplexInfoDTO(Caretaker caretaker);

    @Mapping(target = "accountData", source = "accountData")
    Caretaker mapToCaretaker(ModifyCaretakerDTO caretakerDTO, AppUser accountData);

    @Mapping(target = "animals", source = "caretaker.offers", qualifiedByName = "mapAnimalFromOffer")
    CaretakerDTO mapToCaretakerDTO(Caretaker caretaker);

    default Caretaker updateCaretakerFromDTO(ModifyCaretakerDTO caretakerDTO, @MappingTarget Caretaker caretaker) {
        caretaker.setPhoneNumber(caretakerDTO.phoneNumber());
        caretaker.setDescription(caretakerDTO.description());
        if(caretaker.getAddress() == null) {
            caretaker.setAddress(new Address());
        }
        AddressMapper.INSTANCE.updateAddressFromDTO(caretakerDTO.address(), caretaker.getAddress());

        return caretaker;
    }

    @Named("mapAnimalFromOffer")
    default String mapAnimalFromOffer(Offer offer) {
        return offer.getAnimal().getAnimalType();
    }

}
