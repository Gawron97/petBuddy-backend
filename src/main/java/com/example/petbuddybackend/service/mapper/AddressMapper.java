package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.address.AddressDTO;
import com.example.petbuddybackend.dto.address.UpdateAddressDTO;
import com.example.petbuddybackend.entity.address.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import org.springframework.util.StringUtils;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AddressMapper {

    AddressMapper INSTANCE = Mappers.getMapper(AddressMapper.class);

    @Mapping(target = "id", ignore = true)
    Address mapToAddress(AddressDTO addressDTO);

    AddressDTO mapToAddressDTO(Address address);

    default void updateAddressFromDTO(UpdateAddressDTO addressDTO, @MappingTarget Address address) {
        if(StringUtils.hasText(addressDTO.city())) {
            address.setCity(addressDTO.city());
        }
        if(StringUtils.hasText(addressDTO.zipCode())) {
            address.setZipCode(addressDTO.zipCode());
        }
        if(addressDTO.voivodeship() != null) {
            address.setVoivodeship(addressDTO.voivodeship());
        }
        if(StringUtils.hasText(addressDTO.street())) {
            address.setStreet(addressDTO.street());
        }
        if(StringUtils.hasText(addressDTO.streetNumber())) {
            address.setStreetNumber(addressDTO.streetNumber());
        }
        if(StringUtils.hasText(addressDTO.apartmentNumber())) {
            address.setApartmentNumber(addressDTO.apartmentNumber());
        }
    }

}
