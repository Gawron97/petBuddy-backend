package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.offer.CaretakerOfferDTO;
import com.example.petbuddybackend.dto.paging.PagingParams;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.CaretakerSearchCriteria;
import com.example.petbuddybackend.service.user.CaretakerService;
import com.example.petbuddybackend.utils.paging.PagingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/caretaker")
public class CaretakerController {

    private final CaretakerService caretakerService;

    @SecurityRequirements
    @GetMapping
    @Operation(
            summary = "Get list of caretakers",
            description = "Retrieves a paginated list of caretakers based on provided search criteria and paging parameters."
    )
    public Page<CaretakerDTO> getCaretakers(
            @ParameterObject @ModelAttribute @Valid PagingParams pagingParams,
            @ParameterObject @ModelAttribute CaretakerSearchCriteria filters
    ) {
        Pageable pageable = PagingUtils.createPageable(pagingParams);
        return caretakerService.getCaretakers(pageable, filters);
    }

    @SecurityRequirements
    @Operation(
            summary = "Add or edit caretaker offer",
            description = "Adds or edits a caretaker offer for currently logged caretaker."
    )
    @PostMapping("/offer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CaretakerOfferDTO> addOrEditCaretakerOfferForCaretaker(
            @RequestBody CaretakerOfferDTO caretakerOffer,
            Principal principal
    ) {
        return ResponseEntity.ok(caretakerService.addOrEditCaretakerOfferForCaretaker(caretakerOffer, principal.getName()));
    }

    @SecurityRequirements
    @Operation(
            summary = "Add or edit caretaker offers",
            description = "Adds or edits caretaker offers for currently logged caretaker."
    )
    @PostMapping("/offers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CaretakerOfferDTO>> addOrEditCaretakerOffersForCaretaker(
            @RequestBody List<CaretakerOfferDTO> caretakerOffers,
            Principal principal
    ) {
        return ResponseEntity.ok(caretakerService.addOrEditCaretakerOffersForCaretaker(caretakerOffers, principal.getName()));
    }

}
