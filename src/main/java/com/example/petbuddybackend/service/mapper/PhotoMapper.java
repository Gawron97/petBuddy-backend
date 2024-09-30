package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.photo.PhotoLinkDTO;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PhotoMapper {

    PhotoMapper INSTANCE = Mappers.getMapper(PhotoMapper.class);

    PhotoLinkDTO mapToPhotoLinkDTO(PhotoLink photoLink);
}
