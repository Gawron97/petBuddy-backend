package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.paging.SortedPagingParams;
import com.example.petbuddybackend.dto.rating.RatingRequest;
import com.example.petbuddybackend.dto.rating.RatingResponse;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.rating.RatingService;
import com.example.petbuddybackend.utils.annotation.validation.AcceptRole;
import com.example.petbuddybackend.utils.paging.PagingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rating")
public class RatingController {

    private final RatingService ratingService;

    @SecurityRequirements
    @GetMapping("/{caretakerEmail}")
    @Operation(
            summary = "Get ratings of caretaker",
            description = "Retrieves a paginated list of ratings for a caretaker."
    )
    public Page<RatingResponse> getRatings(
            @ParameterObject @ModelAttribute @Valid SortedPagingParams pagingParams,
            @PathVariable String caretakerEmail
    ) {
        Pageable pageable = PagingUtils.createSortedPageable(pagingParams);
        return ratingService.getRatings(pageable, caretakerEmail);
    }

    @PostMapping("/{careId}")
    @Operation(
            summary = "Rate caretaker",
            description = "Rates a caretaker with a given rating and comment. Updates the rating if it already exists."
    )
    @PreAuthorize("isAuthenticated()")
    public RatingResponse rateCaretaker(
            @RequestHeader(value = "${header-name.role}")
            @AcceptRole(acceptRole = Role.CLIENT)
            Role role,
            @PathVariable Long careId,
            @RequestBody @Valid RatingRequest ratingDTO,
            Principal principal
    ) {
        return ratingService.rateCaretaker(
                principal.getName(),
                careId,
                ratingDTO.rating(),
                ratingDTO.comment()
        );
    }

    @DeleteMapping("/{careId}")
    @Operation(
            summary = "Delete rating",
            description = "Deletes a rating for a caretaker."
    )
    @PreAuthorize("isAuthenticated()")
    public RatingResponse deleteRating(
            @RequestHeader(value = "${header-name.role}")
            @AcceptRole(acceptRole = Role.CLIENT)
            Role role,
            @PathVariable Long careId,
            Principal principal
    ) {
        return ratingService.deleteRating(principal.getName(), careId);
    }

}
