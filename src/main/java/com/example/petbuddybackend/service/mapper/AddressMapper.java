package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.address.AddressDTO;
import com.example.petbuddybackend.dto.address.UpdateAddressDTO;
import com.example.petbuddybackend.entity.address.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AddressMapper {

    AddressMapper INSTANCE = Mappers.getMapper(AddressMapper.class);

    @Mapping(target = "id", ignore = true)
    Address mapToAddress(AddressDTO addressDTO);

    default void updateAddressFromDTO(UpdateAddressDTO addressDTO, @MappingTarget Address address) {
        if(addressDTO.city() != null) {
            address.setCity(addressDTO.city());
        }
        if(addressDTO.zipCode() != null) {
            address.setZipCode(addressDTO.zipCode());
        }
        if(addressDTO.voivodeship() != null) {
            address.setVoivodeship(addressDTO.voivodeship());
        }
        if(addressDTO.street() != null) {
            address.setStreet(addressDTO.street());
        }
        if(addressDTO.buildingNumber() != null) {
            address.setBuildingNumber(addressDTO.buildingNumber());
        }
        if(addressDTO.apartmentNumber() != null) {
            address.setApartmentNumber(addressDTO.apartmentNumber());
        }
    }

}
