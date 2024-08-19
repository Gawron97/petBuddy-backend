package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.offer.OfferConfigurationDTO;
import com.example.petbuddybackend.dto.offer.OfferDTO;
import com.example.petbuddybackend.service.offer.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/caretaker/offer")
public class OfferController {

    private final OfferService offerService;

    @Operation(
            summary = "Add or edit offer",
            description = "Add offer if it does not exists, also can edit offer if it exists by changing offer data" +
                    " and adding new configurations. It Cannot edit configurations of existing offer."
    )
    @PostMapping("/add")
    public OfferDTO addOffer(@RequestBody OfferDTO offer, Principal principal) {
        return offerService.addOrEditOffer(offer, principal.getName());
    }

    @Operation(
            summary = "Edit offer configuration",
            description = "Edits offer configuration by changing configuration data and selected options."
    )
    @PostMapping("/configuration/{configurationId}/edit")
    public OfferConfigurationDTO editConfiguration(@PathVariable Long configurationId,
                                                                   @RequestBody OfferConfigurationDTO configuration) {
        return offerService.editConfiguration(configurationId, configuration);
    }

    @DeleteMapping("/configuration/{configurationId}/delete")
    public OfferDTO deleteConfiguration(@PathVariable Long configurationId) {
        return offerService.deleteConfiguration(configurationId);
    }

}
