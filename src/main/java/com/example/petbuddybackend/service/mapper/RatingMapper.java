package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.rating.RatingResponse;
import com.example.petbuddybackend.entity.rating.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {ClientMapper.class})
public interface RatingMapper {

    RatingMapper INSTANCE = Mappers.getMapper(RatingMapper.class);

    @Mapping(target = "client", source = "care.client")
    @Mapping(target = "caretakerEmail", source = "care.caretaker.email")
    RatingResponse mapToRatingResponse(Rating rating);
}
