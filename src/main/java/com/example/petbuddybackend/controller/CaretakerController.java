package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.criteriaSearch.CaretakerSearchCriteria;
import com.example.petbuddybackend.dto.offer.OfferFilterDTO;
import com.example.petbuddybackend.dto.paging.SortedPagingParams;
import com.example.petbuddybackend.dto.photo.PhotoLinkDTO;
import com.example.petbuddybackend.dto.rating.RatingRequest;
import com.example.petbuddybackend.dto.rating.RatingResponse;
import com.example.petbuddybackend.dto.user.CaretakerComplexDTO;
import com.example.petbuddybackend.dto.user.CaretakerComplexPublicDTO;
import com.example.petbuddybackend.dto.user.CaretakerComplexInfoDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.ModifyCaretakerDTO;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.care.CareService;
import com.example.petbuddybackend.service.user.CaretakerService;
import com.example.petbuddybackend.utils.annotation.validation.AcceptRole;
import com.example.petbuddybackend.utils.paging.PagingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/caretaker")
public class CaretakerController {

    private final CaretakerService caretakerService;
    private final CareService careService;

    @SecurityRequirements
    @PostMapping("/all")
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
    public CaretakerComplexPublicDTO getOtherCaretaker(@PathVariable String caretakerEmail) {
        return caretakerService.getOtherCaretaker(caretakerEmail);
    }

    @GetMapping
    @Operation(
            summary = "Get currently logged in caretaker information",
            description = "Get currently logged in caretaker with details information"
    )
    @PreAuthorize("isAuthenticated()")
    public CaretakerComplexDTO getMyCaretakerProfile(Principal principal,

                                                 @AcceptRole(acceptRole = Role.CARETAKER)
                                            @RequestHeader(value = "${header-name.role}") Role role) {
        return caretakerService.getMyCaretakerProfile(principal.getName());
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(
            summary = "Add caretaker profile",
            description = "Add caretaker profile if it does not exists"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Added caretaker profile successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or image type"),
            @ApiResponse(responseCode = "413", description = "Uploaded file exceeds the maximum allowed size of ${spring.servlet.multipart.max-file-size}")
    })
    @PreAuthorize("isAuthenticated()")
    public CaretakerComplexDTO addCaretaker(
            Principal principal,
            @RequestPart @Valid ModifyCaretakerDTO caretakerData,
            @RequestPart(required = false) Optional<List<@NotNull MultipartFile>> newOfferPhotos
    ) {
        return caretakerService.addCaretaker(
                caretakerData,
                principal.getName(),
                newOfferPhotos.orElse(Collections.emptyList())
        );
    }

    @PutMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(
            summary = "Edit caretaker profile",
            description = """
                    Edit caretaker profile if it does exists.
                    
                    Param newOfferPhotos adds new photos to caretaker profile.
                    
                    Param currentOfferBlobs should contain names (blobs) of photos that are currently in caretaker profile.
                    **Names not included in this set will be removed. Not providing any names will remove all photos**.
                    """
    )
    @PreAuthorize("isAuthenticated()")
    public CaretakerComplexDTO editCaretaker(
            Principal principal,
            @AcceptRole(acceptRole = Role.CARETAKER)
            @RequestHeader(value = "${header-name.role}") Role role,
            @RequestPart @Valid ModifyCaretakerDTO caretakerData,
            @RequestPart(required = false) Optional<List<@NotNull MultipartFile>> newOfferPhotos,
            @RequestPart(required = false) Optional<Set<@NotNull String>> offerBlobsToKeep
    ) {
        return caretakerService.editCaretaker(
                caretakerData,
                principal.getName(),
                offerBlobsToKeep.orElse(Collections.emptySet()),
                newOfferPhotos.orElse(Collections.emptyList())
        );
    }

    @PutMapping(value = "/offer-photo", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Add new offer photos",
            description = """
                    Edit caretaker offer photos
                    Param newOfferPhotos adds new photos to caretaker profile.

                    Param currentOfferBlobs should contain names (blobs) of photos that are currently in caretaker profile.
                    **Names not included in this set will be removed. Not providing any names will remove all photos**.
                    """
    )
    public List<PhotoLinkDTO> editCaretakerOfferPhotos(
            Principal principal,
            @AcceptRole(acceptRole = Role.CARETAKER)
            @RequestHeader(value = "${header-name.role}") Role role,
            @RequestPart(required = false) Optional<Set<@NotNull String>> offerBlobsToKeep,
            @RequestPart(required = false) Optional<List<@NotNull MultipartFile>> newOfferPhotos
    ) {
        return caretakerService.putOfferPhotos(
                principal.getName(),
                offerBlobsToKeep.orElse(Collections.emptySet()),
                newOfferPhotos.orElse(Collections.emptyList())
        );
    }

}
