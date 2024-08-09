package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.offer.CaretakerOfferDTO;
import com.example.petbuddybackend.entity.offer.CaretakerOffer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CaretakerOfferMapper {

    CaretakerOfferMapper INSTANCE = Mappers.getMapper(CaretakerOfferMapper.class);

    CaretakerOfferDTO mapToCaretakerOfferDTO(CaretakerOffer caretakerOffer);

}
