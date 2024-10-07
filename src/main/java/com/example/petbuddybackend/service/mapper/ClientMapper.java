package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.ClientComplexInfoDTO;
import com.example.petbuddybackend.dto.user.ClientDTO;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(uses = {AddressMapper.class, UserMapper.class})
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    ClientDTO mapToClientDTO(Client client);

    @Mapping(target = "followingCaretakersEmails", source = "client.followingCaretakers", qualifiedByName = "mapFollowingCaretakersEmails")
    ClientComplexInfoDTO mapToClientComplexInfoDTO(Client client);

    @Named("mapFollowingCaretakersEmails")
    default Set<String> mapFollowingCaretakersEmails(Set<Caretaker> followingCaretakers) {
        return followingCaretakers
                .stream()
                .map(caretaker -> caretaker.getAccountData().getEmail())
                .collect(Collectors.toSet());
    }

}
