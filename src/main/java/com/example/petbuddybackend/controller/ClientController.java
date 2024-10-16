package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.care.CareDTO;
import com.example.petbuddybackend.dto.care.CreateCareDTO;
import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.ClientDTO;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.care.CareService;
import com.example.petbuddybackend.service.user.ClientService;
import com.example.petbuddybackend.utils.annotation.swaggerdocs.RoleParameter;
import com.example.petbuddybackend.utils.annotation.swaggerdocs.TimeZoneParameter;
import com.example.petbuddybackend.utils.annotation.validation.AcceptRole;
import com.example.petbuddybackend.utils.time.TimeUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/client")
public class ClientController {

    private final ClientService clientService;
    private final CareService careService;

    @Operation(summary = "Get client data")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ClientDTO getClient(Principal principal,

                               @RoleParameter
                               @AcceptRole(acceptRole = Role.CLIENT)
                               @RequestHeader(value = "${header-name.role}") Role role) {
        return clientService.getClient(principal.getName());
    }

    @Operation(summary = "Add caretaker to following list of client")
    @PostMapping("/follow/{caretakerEmail}")
    @PreAuthorize("isAuthenticated()")
    public Set<String> addFollowingCaretakers(Principal principal,
                                              @PathVariable String caretakerEmail,

                                              @RoleParameter
                                              @AcceptRole(acceptRole = Role.CLIENT)
                                              @RequestHeader(value = "${header-name.role}") Role role) {
        return clientService.addFollowingCaretaker(principal.getName(), caretakerEmail);
    }

    @Operation(summary = "Remove caretaker from following list of client")
    @DeleteMapping("/unfollow/{caretakerEmail}")
    @PreAuthorize("isAuthenticated()")
    public Set<String> removeFollowingCaretakers(Principal principal,
                                                 @PathVariable String caretakerEmail,

                                                 @RoleParameter
                                                 @AcceptRole(acceptRole = Role.CLIENT)
                                                 @RequestHeader(value = "${header-name.role}") Role role) {
        return clientService.removeFollowingCaretaker(principal.getName(), caretakerEmail);
    }

    @Operation(summary = "Get followed caretakers of client")
    @GetMapping("/follow")
    @PreAuthorize("isAuthenticated()")
    public Set<AccountDataDTO> getFollowedCaretakers(Principal principal,

                                                     @RoleParameter
                                                   @AcceptRole(acceptRole = Role.CLIENT)
                                                   @RequestHeader(value = "${header-name.role}") Role role) {

        return clientService.getFollowedCaretakers(principal.getName());
    }

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

    @PostMapping("/care/{careId}/accept")
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

    @PostMapping("/care/{careId}/cancel")
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
