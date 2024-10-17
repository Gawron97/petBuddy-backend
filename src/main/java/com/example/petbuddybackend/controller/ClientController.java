package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.ClientDTO;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.care.CareService;
import com.example.petbuddybackend.service.user.ClientService;
import com.example.petbuddybackend.utils.annotation.swaggerdocs.RoleParameter;
import com.example.petbuddybackend.utils.annotation.validation.AcceptRole;
import io.swagger.v3.oas.annotations.Operation;
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
}
