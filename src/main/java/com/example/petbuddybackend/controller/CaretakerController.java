package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.care.CareDTO;
import com.example.petbuddybackend.dto.care.UpdateCareDTO;
import com.example.petbuddybackend.dto.criteriaSearch.CaretakerSearchCriteria;
import com.example.petbuddybackend.dto.offer.OfferFilterDTO;
import com.example.petbuddybackend.dto.paging.SortedPagingParams;
import com.example.petbuddybackend.dto.rating.RatingRequest;
import com.example.petbuddybackend.dto.rating.RatingResponse;
import com.example.petbuddybackend.dto.user.CaretakerComplexInfoDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.ModifyCaretakerDTO;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.care.CareService;
import com.example.petbuddybackend.service.user.CaretakerService;
import com.example.petbuddybackend.utils.annotation.swaggerdocs.TimeZoneParameter;
import com.example.petbuddybackend.utils.annotation.validation.AcceptRole;
import com.example.petbuddybackend.utils.paging.PagingUtils;
import com.example.petbuddybackend.utils.time.TimeUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/caretaker")
public class CaretakerController {

    private final CaretakerService caretakerService;
    private final CareService careService;

    @SecurityRequirements
    @PostMapping
    @Operation(
            summary = "Get list of caretakers",
            description = "Retrieves a paginated list of caretakers based on provided search criteria and paging parameters." +
                    " Request body is not required!" +
                    " When sorting by availabilityDaysMatch this sort will be applied last, so it will be" +
                    " ordered priority by this field"
    )
    public Page<CaretakerDTO> getCaretakers(
            @ParameterObject @ModelAttribute @Valid SortedPagingParams pagingParams,
            @ParameterObject @ModelAttribute CaretakerSearchCriteria filters,
            @RequestBody(required = false) Set<@Valid OfferFilterDTO> offerFilters
            ) {
        if(offerFilters == null) {
            offerFilters = Collections.emptySet();
        }

        Pageable pageable = PagingUtils.createSortedPageable(pagingParams);
        return caretakerService.getCaretakers(pageable, filters, offerFilters);
    }

    @SecurityRequirements
    @GetMapping("/{caretakerEmail}")
    @Operation(
            summary = "Get caretaker information",
            description = "Get caretaker with details information"
    )
    public CaretakerComplexInfoDTO getCaretaker(@PathVariable String caretakerEmail) {
        return caretakerService.getCaretaker(caretakerEmail);
    }

    @PostMapping("/add")
    @Operation(
            summary = "Add caretaker profile",
            description = "Add caretaker profile if it does not exists"
    )
    @PreAuthorize("isAuthenticated()")
    public CaretakerComplexInfoDTO addCaretaker(
            @RequestBody @Valid ModifyCaretakerDTO caretakerDTO,
            Principal principal
    ) {
        return caretakerService.addCaretaker(caretakerDTO, principal.getName());
    }

    @PutMapping("/edit")
    @Operation(
            summary = "Edit caretaker profile",
            description = "Edit caretaker profile if it does exists"
    )
    @PreAuthorize("isAuthenticated()")
    public CaretakerComplexInfoDTO editCaretaker(
            @RequestBody @Valid ModifyCaretakerDTO caretakerDTO,
            Principal principal
    ) {
        return caretakerService.editCaretaker(caretakerDTO, principal.getName());
    }

    @SecurityRequirements
    @GetMapping("/{caretakerEmail}/rating")
    @Operation(
            summary = "Get ratings of caretaker",
            description = "Retrieves a paginated list of ratings for a caretaker."
    )
    public Page<RatingResponse> getRatings(
            @ParameterObject @ModelAttribute @Valid SortedPagingParams pagingParams,
            @PathVariable String caretakerEmail
    ) {
        Pageable pageable = PagingUtils.createSortedPageable(pagingParams);
        return caretakerService.getRatings(pageable, caretakerEmail);
    }

    @PostMapping("/{caretakerEmail}/rating")
    @Operation(
            summary = "Rate caretaker",
            description = "Rates a caretaker with a given rating and comment. Updates the rating if it already exists."
    )
    @PreAuthorize("isAuthenticated()")
    public RatingResponse rateCaretaker(
            @PathVariable String caretakerEmail,
            @RequestBody @Valid RatingRequest ratingDTO,
            Principal principal
    ) {
        return caretakerService.rateCaretaker(
                caretakerEmail,
                principal.getName(),
                ratingDTO.rating(),
                ratingDTO.comment()
        );
    }

    @DeleteMapping("/{caretakerEmail}/rating")
    @Operation(
            summary = "Delete rating",
            description = "Deletes a rating for a caretaker."
    )
    @PreAuthorize("isAuthenticated()")
    public RatingResponse deleteRating(
            @PathVariable String caretakerEmail,
            Principal principal
    ) {
        return caretakerService.deleteRating(caretakerEmail, principal.getName());
    }

    @PatchMapping("/care/{careId}")
    @Operation(
            summary = "Update a care",
            description = "Updates an existing care with new data. " +
                    "The reservation must be in the PENDING status for caretaker to edit." +
                    "After editing client status changes to PENDING and need to accept new changes."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Care edited successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Authorized caretaker can only edit care"),
            @ApiResponse(responseCode = "404", description = "When data provided is not found in the system")
    })
    @PreAuthorize("isAuthenticated()")
    public CareDTO updateCare(
            @RequestHeader(value = "${header-name.role}")
            @AcceptRole(acceptRole = Role.CARETAKER)
            Role role,
            @PathVariable Long careId,
            @RequestBody @Valid UpdateCareDTO updateCare,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
            Principal principal
    ) {
        return careService.updateCare(careId, updateCare, principal.getName(), TimeUtils.getOrSystemDefault(timeZone));
    }

    @PostMapping("/care/{careId}/accept")
    @Operation(
            summary = "Accept a care by caretaker",
            description = "Accepts a care by caretaker. " +
                    "The care must be in the PENDING status for caretaker and ACCEPT status for client to accept." +
                    "When successful both statuses changes to AWAITING_PAYMENT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Care accepted by caretaker successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Authorized caretaker can only accept care"),
            @ApiResponse(responseCode = "404", description = "When data provided is not found in the system")
    })
    @PreAuthorize("isAuthenticated()")
    public CareDTO acceptCareByCaretaker(
            @RequestHeader(value = "${header-name.role}")
            @AcceptRole(acceptRole = Role.CARETAKER)
            Role role,
            @PathVariable Long careId,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
            Principal principal) {
        return careService.acceptCareByCaretaker(careId, principal.getName(), TimeUtils.getOrSystemDefault(timeZone));
    }

    @PostMapping("/care/{careId}/reject")
    @Operation(
            summary = "Reject a care by caretaker",
            description = "Rejects a care by caretaker. " +
                    "The care cannot be accepted by caretaker to reject."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Care rejected by caretaker successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Authorized caretaker can only reject care"),
            @ApiResponse(responseCode = "404", description = "When data provided is not found in the system")
    })
    @PreAuthorize("isAuthenticated()")
    public CareDTO rejectCareByCaretaker(
            @RequestHeader(value = "${header-name.role}")
            @AcceptRole(acceptRole = Role.CARETAKER)
            Role role,
            @PathVariable Long careId,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
            Principal principal) {
        return careService.rejectCareByCaretaker(careId, principal.getName(), TimeUtils.getOrSystemDefault(timeZone));
    }
}
