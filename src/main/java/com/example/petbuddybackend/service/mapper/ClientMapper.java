package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.ClientDTO;
import com.example.petbuddybackend.entity.user.Client;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = AddressMapper.class)
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    // TODO test
    ClientDTO mapToClientDTO(Client client);

}
