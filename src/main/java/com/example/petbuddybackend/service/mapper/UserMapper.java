package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.UserProfilesData;
import com.example.petbuddybackend.entity.user.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {PhotoMapper.class})
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "accountData", source = "appUser")
    UserProfilesData mapToProfileData(AppUser appUser, Boolean hasClientProfile, Boolean hasCaretakerProfile);

    AccountDataDTO mapToAccountDataDTO(AppUser appUser);
}
