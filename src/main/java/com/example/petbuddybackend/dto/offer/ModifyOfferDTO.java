package com.example.petbuddybackend.dto.offer;

import com.example.petbuddybackend.dto.animal.AnimalDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record ModifyOfferDTO(
        String description,

        @NotNull
        @Valid
        AnimalDTO animal,
        List<@Valid ModifyConfigurationDTO> offerConfigurations,
        List<String> animalAmenities
) {
}
