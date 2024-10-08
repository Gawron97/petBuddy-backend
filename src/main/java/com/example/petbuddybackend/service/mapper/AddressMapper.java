package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.address.AddressDTO;
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

    AddressDTO mapToAddressDTO(Address address);

    void updateAddressFromDTO(AddressDTO addressDTO, @MappingTarget Address address);

}
