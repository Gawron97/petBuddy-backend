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
}
