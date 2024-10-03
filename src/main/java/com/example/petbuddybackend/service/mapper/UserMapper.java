package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.ProfileData;
import com.example.petbuddybackend.entity.user.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {PhotoMapper.class})
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "accountData", source = "appUser")
    ProfileData mapToProfileData(AppUser appUser, Boolean hasClientProfile, Boolean hasCaretakerProfile);

    AccountDataDTO mapToAccountDataDTO(AppUser appUser);
}
