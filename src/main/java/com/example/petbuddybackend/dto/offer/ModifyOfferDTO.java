package com.example.petbuddybackend.dto.offer;

import com.example.petbuddybackend.dto.animal.AnimalDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record ModifyOfferDTO(
        @Schema(description = "Description of the offer", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String description,

        @NotNull
        @Valid
        AnimalDTO animal,
        List<@Valid ModifyConfigurationDTO> offerConfigurations,
        List<String> animalAmenities
) {
}
