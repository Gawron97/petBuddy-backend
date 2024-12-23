package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.care.CreateCareDTO;
import com.example.petbuddybackend.dto.care.DetailedCareDTO;
import com.example.petbuddybackend.dto.care.DetailedCareWithHistoryDTO;
import com.example.petbuddybackend.dto.care.UpdateCareDTO;
import com.example.petbuddybackend.dto.criteriaSearch.CareSearchCriteria;
import com.example.petbuddybackend.dto.criteriaSearch.CareStatisticsSearchCriteria;
import com.example.petbuddybackend.dto.paging.PagingParams;
import com.example.petbuddybackend.dto.paging.SortedPagingParams;
import com.example.petbuddybackend.dto.statistic.MonthlyRevenueDTO;
import com.example.petbuddybackend.dto.user.SimplifiedAccountDataDTO;
import com.example.petbuddybackend.entity.care.CareStatus;
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
import java.time.ZoneId;
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
                            You can sort by every param in DTO excluding selectedOptions,
                            but with some fields it will be different format:
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
    public Page<DetailedCareDTO> getCares(
            @RoleParameter @RequestHeader(value = "${header-name.role}") Role acceptRole,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
            @ParameterObject @ModelAttribute @Valid SortedPagingParams pagingParams,
            @ParameterObject @ModelAttribute @Valid CareSearchCriteria filters,
            @RequestParam(required = false) Set<String> emails,
            Principal principal
    ) {

        Pageable pageable = PagingUtils.createSortedPageable(pagingParams);
        return careService.getCares(
                pageable, filters, emails, principal.getName(), acceptRole, TimeUtils.getOrSystemDefault(timeZone)
        );
    }

    @GetMapping("/{careId}")
    @Operation(summary = "Get specified care info")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Care fetched successfully"),
            @ApiResponse(responseCode = "403", description = "User not part of care"),
            @ApiResponse(responseCode = "404", description = "When data provided is not found in the system")
    })
    @PreAuthorize("isAuthenticated()")
    public DetailedCareWithHistoryDTO getCare(
            @PathVariable Long careId,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
            Principal principal
    ) {
        return careService.getCare(careId, TimeUtils.getOrSystemDefault(timeZone), principal.getName());
    }

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
    public DetailedCareWithHistoryDTO changeCarePrice(
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

    @PostMapping("/{careId}/confirm")
    @Operation(
            summary = "Mark care as confirmed",
            description = """
                    Caretaker marks care as confirmed.
                    
                    The care must be in the READY_TO_PROCEED status for caretaker to confirmed.
                    When successful both statuses changes to CONFIRMED.
                    
                    The care must be marked as confirmed by caretaker the same day the care starts.
                    """
    )
    @PreAuthorize("isAuthenticated()")
    public DetailedCareWithHistoryDTO markCareAsConfirmed(
            @AcceptRole(acceptRole = Role.CARETAKER)
            @RequestHeader(value = "${header-name.role}") Role role,
            Principal principal,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
            @PathVariable Long careId
    ) {
        return careService.markCareAsConfirmed(
                careId,
                principal.getName(),
                role,
                TimeUtils.getOrSystemDefault(timeZone)
        );
    }


    @PostMapping("/{careId}/accept")
    @Operation(
            summary = "Accept a care",
            description = """
                    Caretaker accepts a reservation that client sends and client accepts the care that was edited by
                    caretaker.
                    
                    The care must be in the PENDING status for caretaker and ACCEPT status for client to accept.
                    When successful both statuses changes to READY_TO_PROCEED.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Care accepted by caretaker successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Authorized caretaker can only accept care"),
            @ApiResponse(responseCode = "404", description = "When data provided is not found in the system")
    })
    @PreAuthorize("isAuthenticated()")
    public DetailedCareWithHistoryDTO acceptCare(
            @RequestHeader(value = "${header-name.role}")
            @RoleParameter
            Role role,
            @PathVariable Long careId,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
            Principal principal
    ) {
        ZoneId timezone = TimeUtils.getOrSystemDefault(timeZone);

        return role.equals(Role.CARETAKER) ?
                careService.caretakerChangeCareStatus(careId, principal.getName(), timezone, CareStatus.ACCEPTED) :
                careService.clientChangeCareStatus(careId, principal.getName(), timezone, CareStatus.ACCEPTED);
    }

    @PostMapping("/{careId}/reject")
    @Operation(
            summary = "Reject a care",
            description = "Rejects a care."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Care rejected by caretaker successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Authorized caretaker can only reject care"),
            @ApiResponse(responseCode = "404", description = "When data provided is not found in the system")
    })
    @PreAuthorize("isAuthenticated()")
    public DetailedCareWithHistoryDTO rejectCareByCaretaker(
            @RequestHeader(value = "${header-name.role}")
            @RoleParameter
            Role role,
            @PathVariable Long careId,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
            Principal principal
    ) {
        ZoneId timezone = TimeUtils.getOrSystemDefault(timeZone);

        return role.equals(Role.CARETAKER) ?
                careService.caretakerChangeCareStatus(careId, principal.getName(), timezone, CareStatus.CANCELLED) :
                careService.clientChangeCareStatus(careId, principal.getName(), timezone, CareStatus.CANCELLED);
    }

    @PostMapping("/{caretakerEmail}")
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
    public DetailedCareWithHistoryDTO makeReservation(
            @AcceptRole(acceptRole = Role.CLIENT)
            @RequestHeader(value = "${header-name.role}") Role role,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
            @RequestBody @Valid CreateCareDTO createCare,
            @PathVariable String caretakerEmail,
            Principal principal
    ) {
        return careService.makeReservation(
                createCare,
                principal.getName(),
                caretakerEmail,
                TimeUtils.getOrSystemDefault(timeZone)
        );
    }

    @GetMapping("/related-users")
    @Operation(
            summary = "Get users data related to your cares"
    )
    @PreAuthorize("isAuthenticated()")
    public Page<SimplifiedAccountDataDTO> getUsersRelatedToYourCares(
            Principal principal,
            @ParameterObject @ModelAttribute @Valid PagingParams pagingParams,
            @RequestHeader(value = "${header-name.role}") Role role
    ) {
        Pageable pageable = PagingUtils.createPageable(pagingParams);
        return careService.getUsersRelatedToYourCares(principal.getName(), pageable, role);
    }

    @GetMapping("/monthly-revenue")
    @Operation(
            summary = "Get monthly revenue for the logged in user",
            description = "Returns monthly revenue from cares for logged in user"
    )
    @PreAuthorize("isAuthenticated()")
    public MonthlyRevenueDTO getMonthlyRevenue(Principal principal,

                                               @ParameterObject @ModelAttribute @Valid CareStatisticsSearchCriteria filters,
                                               @RequestParam(required = false) Set<String> emails,
                                               @RequestHeader(value = "${header-name.role}") @AcceptRole(acceptRole = Role.CARETAKER) Role role) {
        return careService.getMonthlyRevenue(principal.getName(), filters, emails);
    }

}
