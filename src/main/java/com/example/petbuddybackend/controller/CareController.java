package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.care.CareDTO;
import com.example.petbuddybackend.dto.care.CreateCareDTO;
import com.example.petbuddybackend.dto.care.UpdateCareDTO;
import com.example.petbuddybackend.dto.criteriaSearch.CareSearchCriteria;
import com.example.petbuddybackend.dto.paging.SortedPagingParams;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.care.CareService;
import com.example.petbuddybackend.utils.annotations.swaggerdocs.RoleParameter;
import com.example.petbuddybackend.utils.annotations.swaggerdocs.TimeZoneParameter;
import com.example.petbuddybackend.utils.annotations.validation.AcceptRole;
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

    @PostMapping("/reservation")
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
    public CareDTO makeReservation(@RequestBody @Valid CreateCareDTO createCare,
                                   @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
                                   Principal principal) {
        return careService.makeReservation(createCare, principal.getName(), TimeUtils.getOrSystemDefault(timeZone));
    }

    @PatchMapping("/{careId}/update")
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
    public CareDTO updateCare(@PathVariable Long careId,
                              @RequestBody @Valid UpdateCareDTO updateCare,
                              @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
                              Principal principal) {
        return careService.updateCare(careId, updateCare, principal.getName(), TimeUtils.getOrSystemDefault(timeZone));
    }

    @PostMapping("/{careId}/caretaker-accept")
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
    public CareDTO acceptCareByCaretaker(@PathVariable Long careId,
                                         @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
                                         Principal principal) {
        return careService.acceptCareByCaretaker(careId, principal.getName(), TimeUtils.getOrSystemDefault(timeZone));
    }

    @PostMapping("/{careId}/client-accept")
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
    public CareDTO acceptCareByClient(@PathVariable Long careId,
                                      @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
                                      Principal principal) {
        return careService.acceptCareByClient(careId, principal.getName(), TimeUtils.getOrSystemDefault(timeZone));
    }

    @PostMapping("/{careId}/caretaker-reject")
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
    public CareDTO rejectCareByCaretaker(@PathVariable Long careId,
                                         @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
                                         Principal principal) {
        return careService.rejectCareByCaretaker(careId, principal.getName(), TimeUtils.getOrSystemDefault(timeZone));
    }

    @PostMapping("/{careId}/client-cancel")
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
    public CareDTO cancelCareByClient(@PathVariable Long careId,
                                      @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
                                      Principal principal) {
        return careService.cancelCareByClient(careId, principal.getName(), TimeUtils.getOrSystemDefault(timeZone));
    }

    @GetMapping("/caretaker-cares")
    @Operation(
            summary = "Get filtered cares for caretaker",
            description = "Returns filtered cares for caretaker. " +
                    "The result is paginated, sorted and filtered by the provided parameters."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cares fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Authorized caretaker can only fetch cares"),
            @ApiResponse(responseCode = "404", description = "When data provided is not found in the system")
    })
    public Page<CareDTO> getCaretakerCares(
            @RoleParameter @AcceptRole(acceptRole = "CARETAKER") @RequestHeader(value = "${header-name.role}") Role acceptRole,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String timeZone,
            @ParameterObject @ModelAttribute @Valid SortedPagingParams pagingParams,
            @ParameterObject @ModelAttribute @Valid CareSearchCriteria filters,
            @RequestParam(required = false) Set<String> clientEmails,
            Principal principal) {

        Pageable pageable = PagingUtils.createSortedPageable(pagingParams);
        return careService.getCaretakerCares(pageable, filters, clientEmails, principal.getName(), TimeUtils.getOrSystemDefault(timeZone));

    }

}
