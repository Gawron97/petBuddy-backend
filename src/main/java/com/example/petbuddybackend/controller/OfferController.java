package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.availability.CreateOffersAvailabilityDTO;
import com.example.petbuddybackend.dto.offer.ModifyConfigurationDTO;
import com.example.petbuddybackend.dto.offer.ModifyOfferDTO;
import com.example.petbuddybackend.dto.offer.OfferConfigurationDTO;
import com.example.petbuddybackend.dto.offer.OfferDTO;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.offer.OfferService;
import com.example.petbuddybackend.utils.annotation.validation.AcceptRole;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/caretaker/offer")
public class OfferController {

    private final OfferService offerService;

    @Operation(
            summary = "Add or edit offer",
            description = "Add offer if it does not exists," +
                    " also can edit offer if it exists. Editing only support adding new configurations or amenities" +
                    " when provide amenity or configuration that already exists throws error" +
                    " For editing or removing configuration use /configuration/{configurationId}/edit" +
                    " or /configuration/{configurationId}/delete endpoints."
    )
    @PostMapping("/add-or-edit")
    @PreAuthorize("isAuthenticated()")
    public OfferDTO addOrEditOffer(@RequestBody @Valid ModifyOfferDTO offer,
                                   Principal principal,
                                   @AcceptRole(acceptRole = Role.CARETAKER)
                                   @RequestHeader(value = "${header-name.role}") Role role) {
        return offerService.addOrEditOffer(offer, principal.getName());
    }

    @Operation(
            summary = "Edit offer configuration",
            description = "Edits offer configuration by changing configuration data and selected options." +
                    " After editing in configuration exists only provided options. So if option not provided but" +
                    " exists in configuration, it will be removed. If option provided but not exists in configuration," +
                    " it will be added"
    )
    @PostMapping("/configuration/{configurationId}/edit")
    @PreAuthorize("isAuthenticated()")
    public OfferConfigurationDTO editConfiguration(@PathVariable Long configurationId,
                                                   @RequestBody @Valid ModifyConfigurationDTO configuration,
                                                   Principal principal,
                                                   @AcceptRole(acceptRole = Role.CARETAKER)
                                                   @RequestHeader(value = "${header-name.role}") Role role) {
        return offerService.editConfiguration(configurationId, configuration, principal.getName());
    }

    @Operation(
            summary = "Delete configuration",
            description = "Deletes configuration from offer"
    )
    @DeleteMapping("/configuration/{configurationId}/delete")
    @PreAuthorize("isAuthenticated()")
    public OfferDTO deleteConfiguration(@PathVariable Long configurationId,
                                        Principal principal,
                                        @AcceptRole(acceptRole = Role.CARETAKER)
                                        @RequestHeader(value = "${header-name.role}") Role role) {
        return offerService.deleteConfiguration(configurationId, principal.getName());
    }

    @Operation(summary = "Delete amenities from offer")
    @PostMapping("/{offerId}/amenities-delete")
    @PreAuthorize("isAuthenticated()")
    public OfferDTO deleteAmenitiesFromOffer(@RequestBody List<String> amenities,
                                             @PathVariable Long offerId,
                                             Principal principal,
                                             @AcceptRole(acceptRole = Role.CARETAKER)
                                             @RequestHeader(value = "${header-name.role}") Role role) {
        return offerService.deleteAmenitiesFromOffer(amenities, principal.getName(), offerId);
    }

    @Operation(
            summary = "Set availability for offers",
            description = "Set availability for offers. If there was availability set before, it will be replaced."
    )
    @PostMapping("/set-availability")
    @PreAuthorize("isAuthenticated()")
    public List<OfferDTO> setAvailabilityForOffers(
            @RequestBody @Valid CreateOffersAvailabilityDTO createOffersAvailability,
            Principal principal,
            @AcceptRole(acceptRole = Role.CARETAKER)
            @RequestHeader(value = "${header-name.role}") Role role) {
        return offerService.setAvailabilityForOffers(createOffersAvailability, principal.getName());
    }

}
