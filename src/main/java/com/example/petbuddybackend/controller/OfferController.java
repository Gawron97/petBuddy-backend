package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.availability.CreateOffersAvailabilityDTO;
import com.example.petbuddybackend.dto.offer.ModifyConfigurationDTO;
import com.example.petbuddybackend.dto.offer.ModifyOfferDTO;
import com.example.petbuddybackend.dto.offer.OfferConfigurationDTO;
import com.example.petbuddybackend.dto.offer.OfferDTO;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.offer.OfferService;
import com.example.petbuddybackend.utils.annotation.validation.AcceptRole;
import com.example.petbuddybackend.utils.annotation.swaggerdocs.RoleParameter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;

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
                    " For editing or removing configuration use /configuration/{configurationId}" +
                    " or /configuration/{configurationId} endpoints."
    )
    @PostMapping("/add-or-edit")
    @PreAuthorize("isAuthenticated()")
    public OfferDTO addOrEditOffer(@RequestBody @Valid ModifyOfferDTO offer,
                                   Principal principal,
                                   @AcceptRole(acceptRole = Role.CARETAKER)
                                   @RequestHeader(value = "${header-name.role}") Role role) {
        return offerService.addOrEditOffer(offer, principal.getName());
    }

    @DeleteMapping("/{offerId}")
    @PreAuthorize("isAuthenticated()")
    public OfferDTO deleteOffer(@PathVariable Long offerId,
                                Principal principal,

                                @RoleParameter
                                @AcceptRole(acceptRole = Role.CARETAKER)
                                @RequestHeader(value = "${header-name.role}") Role role) {
        return offerService.deleteOffer(offerId, principal.getName());
    }

    @Operation(
            summary = "Add configurations for offer",
            description = "Adds configurations for offer. If configuration already exists, it will throw exception." +
                    " If configuration does not exists, it will be added."
    )
    @PostMapping("/{offerId}/configurations")
    @PreAuthorize("isAuthenticated()")
    public OfferDTO addConfigurationsForOffer(@PathVariable Long offerId,
                                              @RequestBody List<@Valid ModifyConfigurationDTO> configurations,
                                              Principal principal,

                                              @AcceptRole(acceptRole = Role.CARETAKER)
                                              @RequestHeader(value = "${header-name.role}") Role role) {

        return offerService.addConfigurationsForOffer(offerId, configurations, principal.getName());

    }

    @Operation(
            summary = "Set amenities for offer",
            description = "Set amenities for offer. If provided amenity already exists in offer it will be skipped," +
                    " only new amenities will be added. If amenity does exists in offer, but not provided in request," +
                    " it will be removed."
    )
    @PutMapping("/{offerId}/amenities")
    @PreAuthorize("isAuthenticated()")
    public OfferDTO setAmenitiesForOffer(@PathVariable Long offerId,
                                              @RequestBody Set<String> amenities,
                                              Principal principal,

                                              @RoleParameter
                                              @AcceptRole(acceptRole = Role.CARETAKER)
                                              @RequestHeader(value = "${header-name.role}") Role role) {

        return offerService.setAmenitiesForOffer(offerId, amenities, principal.getName());

    }

    @Operation(
            summary = "Edit offer configuration",
            description = "Edits offer configuration by changing configuration data and selected options." +
                    " After editing in configuration exists only provided options. So if option not provided but" +
                    " exists in configuration, it will be removed. If option provided but not exists in configuration," +
                    " it will be added"
    )
    @PutMapping("/configuration/{configurationId}")
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
    @DeleteMapping("/configuration/{configurationId}")
    @PreAuthorize("isAuthenticated()")
    public OfferDTO deleteConfiguration(@PathVariable Long configurationId,
                                        Principal principal,
                                        @AcceptRole(acceptRole = Role.CARETAKER)
                                        @RequestHeader(value = "${header-name.role}") Role role) {
        return offerService.deleteConfiguration(configurationId, principal.getName());
    }

    @Operation(summary = "Delete amenities from offer")
    @DeleteMapping("/{offerId}/amenities")
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
    @PutMapping("/availability")
    @PreAuthorize("isAuthenticated()")
    public List<OfferDTO> setAvailabilityForOffers(
            @RequestBody @Valid CreateOffersAvailabilityDTO createOffersAvailability,
            Principal principal,
            @AcceptRole(acceptRole = Role.CARETAKER)
            @RequestHeader(value = "${header-name.role}") Role role) {
        return offerService.setAvailabilityForOffers(createOffersAvailability, principal.getName());
    }

}
