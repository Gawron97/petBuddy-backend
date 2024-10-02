package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.ClientDTO;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.user.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {AddressMapper.class, UserMapper.class})
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);
    UserMapper userMapper = UserMapper.INSTANCE;

    @Mapping(target = "accountData", expression = "java(userMapper.mapToAccountDataDTO(client.getAccountData(), profilePicture))")
    ClientDTO mapToClientDTO(Client client, PhotoLink profilePicture);

}
