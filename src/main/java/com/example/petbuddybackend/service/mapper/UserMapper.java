package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.ProfileData;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.user.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {PhotoMapper.class})
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "accountData", expression = "java(mapToAccountDataDTO(appUser, profilePicture))")
    ProfileData mapToProfileData(AppUser appUser, PhotoLink profilePicture, Boolean hasClientProfile, Boolean hasCaretakerProfile);

    default AccountDataDTO mapToAccountDataDTO(AppUser appUser, PhotoLink profilePicture) {
        return new AccountDataDTO(
                appUser.getEmail(),
                appUser.getName(),
                appUser.getSurname(),
                PhotoMapper.INSTANCE.mapToPhotoLinkDTO(profilePicture)
        );
    }
}
