package com.example.petbuddybackend.dto.user;

import com.example.petbuddybackend.dto.address.AddressDTO;
import com.example.petbuddybackend.dto.animal.AnimalDTO;
import lombok.Builder;
import java.util.List;

@Builder
public record CaretakerDTO (
    AccountDataDTO accountData,
    String phoneNumber,
    String description,
    AddressDTO address,
    List<AnimalDTO> animalsTakenCareOf,
    Float avgRating
){}
