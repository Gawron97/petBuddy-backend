package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.ProfileData;
import com.example.petbuddybackend.entity.user.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "user", target = "accountData")
    ProfileData mapToProfileData(AppUser user, Boolean hasClientProfile, Boolean hasCaretakerProfile);
}
