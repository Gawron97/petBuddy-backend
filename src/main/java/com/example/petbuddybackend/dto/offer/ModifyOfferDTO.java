package com.example.petbuddybackend.dto.offer;

import com.example.petbuddybackend.dto.animal.AnimalDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
import java.util.Set;

@Builder
public record ModifyOfferDTO(
        String description,

        @NotNull
        @Valid
        AnimalDTO animal,
        List<@Valid ModifyConfigurationDTO> offerConfigurations,
        Set<String> animalAmenities
) {
}
