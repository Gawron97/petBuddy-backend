package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.care.CareDTO;
import com.example.petbuddybackend.dto.care.CreateCareDTO;
import com.example.petbuddybackend.dto.care.UpdateCareDTO;
import com.example.petbuddybackend.dto.criteriaSearch.CareSearchCriteria;
import com.example.petbuddybackend.dto.paging.SortedPagingParams;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.care.CareService;
import com.example.petbuddybackend.utils.annotation.swaggerdocs.RoleParameter;
import com.example.petbuddybackend.utils.annotation.swaggerdocs.TimeZoneParameter;
import com.example.petbuddybackend.utils.annotation.validation.AcceptRole;
import com.example.petbuddybackend.utils.paging.PagingUtils;
import com.example.petbuddybackend.utils.time.TimeUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Set;

@RestController
@RequestMapping("/api/care")
@RequiredArgsConstructor
public class CareController {

    private final CareService careService;

    @GetMapping
    @Operation(
            summary = "Get filtered cares for currently logged in user profile",
            description =
                    """
                            ## Endpoint description
                            Returns filtered cares for currently logged in user profile.
                            The result is paginated, sorted and filtered by the provided parameters.
                            
                            ## Sorting
                            You can sort by every param in DTO excluding selectedOptions, but with some fields it will be different formatt:
                            - To sort by animalType you need to provide: `animal_animalType`
                            - To sort by caretakerEmail you need to provide: `caretaker_email`
                            - To sort by clientEmail you need to provide: `client_email`
                            
                            ## Filtering
                            Filtering by emails:
                            - when user is logged in CARETAKER profile, you can provide client emails to filter by
                            - when user is logged in CLIENT profile, you can provide caretaker emails to filter by
                            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cares fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Authorized user can only fetch cares"),
    })
    @PreAuthorize("isAuthenticated()")
    public Page<CareDTO> getCares(
            @RoleParameter @RequestHeader(value = "${header-name.role}") Role acceptRole,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
            @ParameterObject @ModelAttribute @Valid SortedPagingParams pagingParams,
            @ParameterObject @ModelAttribute @Valid CareSearchCriteria filters,
            @RequestParam(required = false) Set<String> emails,
            Principal principal
    ) {

        Pageable pageable = PagingUtils.createSortedPageable(pagingParams);
        return careService.getCares(pageable, filters, emails, principal.getName(), acceptRole, TimeUtils.getOrSystemDefault(timeZone));
    }

    // CARETAKER ENDPOINTS
    @PatchMapping("/{careId}")
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

    @PostMapping("/{careId}/accept")
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

    @PostMapping("/{careId}/reject")
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

    // CLIENT ENDPOINTS
    // TODO: mark cares as CANCELLED on block?
    @PostMapping("/care")
    @Operation(
            summary = "Make a reservation for a pet care service",
            description = "Creates a new care reservation for a pet care service. " +
                    "The reservation is created in the PENDING status for caretaker and must be accepted or edited."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Care reservation created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Authorized client can only make reservation for themselves"),
            @ApiResponse(responseCode = "404", description = "When data provided is not found in the system")
    })
    @PreAuthorize("isAuthenticated()")
    public CareDTO makeReservation(
            @RequestHeader(value = "${header-name.role}")
            @AcceptRole(acceptRole = Role.CLIENT)
            Role role,
            @RequestBody @Valid CreateCareDTO createCare,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
            Principal principal
    ) {
        return careService.makeReservation(createCare, principal.getName(), TimeUtils.getOrSystemDefault(timeZone));
    }

    @PostMapping("/{careId}/accept")
    @Operation(
            summary = "Accept a care by client",
            description = "Accepts a care by client. " +
                    "The care must be in the PENDING status for client to accept."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Care accepted by client successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Authorized client can only accept care"),
            @ApiResponse(responseCode = "404", description = "When data provided is not found in the system")
    })
    @PreAuthorize("isAuthenticated()")
    public CareDTO acceptCareByClient(
            @RequestHeader(value = "${header-name.role}")
            @AcceptRole(acceptRole = Role.CLIENT)
            Role role,
            @PathVariable Long careId,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
            Principal principal) {
        return careService.acceptCareByClient(careId, principal.getName(), TimeUtils.getOrSystemDefault(timeZone));
    }

    @PostMapping("/{careId}/cancel")
    @Operation(
            summary = "Cancel a care by client",
            description = "Cancels a care by client. " +
                    "The care cannot be accepted by caretaker to cancel."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Care cancelled by caretaker successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Authorized client can only cancel care"),
            @ApiResponse(responseCode = "404", description = "When data provided is not found in the system")
    })
    @PreAuthorize("isAuthenticated()")
    public CareDTO cancelCareByClient(
            @RequestHeader(value = "${header-name.role}")
            @AcceptRole(acceptRole = Role.CLIENT)
            Role role,
            @PathVariable Long careId,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
            Principal principal) {
        return careService.cancelCareByClient(careId, principal.getName(), TimeUtils.getOrSystemDefault(timeZone));
    }
}
