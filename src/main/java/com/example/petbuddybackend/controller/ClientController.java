package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.user.ClientDTO;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.user.ClientService;
import com.example.petbuddybackend.utils.annotation.swaggerdocs.RoleParameter;
import com.example.petbuddybackend.utils.annotation.validation.AcceptRole;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client")
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "Get client data")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ClientDTO getClient(Principal principal,
                               @RoleParameter
                                   @AcceptRole(acceptRole = Role.CLIENT)
                                   @RequestHeader(value = "${header-name.role}") Role role) {
        return clientService.getClient(principal.getName());
    }

}
