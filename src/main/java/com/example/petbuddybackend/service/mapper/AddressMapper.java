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

    default void updateAddressFromDTO(AddressDTO addressDTO, @MappingTarget Address address) {
        address.setCity(addressDTO.city());
        address.setZipCode(addressDTO.zipCode());
        address.setVoivodeship(addressDTO.voivodeship());
        address.setStreet(addressDTO.street());
        address.setStreetNumber(addressDTO.streetNumber());
        address.setApartmentNumber(addressDTO.apartmentNumber());
    }

}
