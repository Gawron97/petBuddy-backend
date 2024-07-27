package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.paging.PagingParams;
import com.example.petbuddybackend.dto.rating.RatingDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.CaretakerSearchCriteria;
import com.example.petbuddybackend.service.user.CaretakerService;
import com.example.petbuddybackend.utils.paging.PagingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/caretaker")
public class CaretakerController {

    private final CaretakerService caretakerService;

    @SecurityRequirements
    @GetMapping
    @Operation(
            summary = "Get list of caretakers",
            description = "Retrieves a paginated list of caretakers based on provided search criteria and paging parameters."
    )
    public Page<CaretakerDTO> getCaretakers(
            @ParameterObject @ModelAttribute @Valid PagingParams pagingParams,
            @ParameterObject @ModelAttribute CaretakerSearchCriteria filters
    ) {
        Pageable pageable = PagingUtils.createPageable(pagingParams);
        return caretakerService.getCaretakers(pageable, filters);
    }

    @PostMapping("/{caretakerId}/rate")
    @Operation(
            summary = "Rate caretaker",
            description = "Rates a caretaker with a given rating and comment."
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rateCaretaker(
            @PathVariable Long caretakerId,
            @RequestBody @Valid RatingDTO ratingDTO,
            Principal principal
    ) {
        caretakerService.rateCaretaker(caretakerId, principal.getName(), ratingDTO.rating(), ratingDTO.comment());
    }

    @DeleteMapping("/{caretakerId}/rate")
    @Operation(
            summary = "Delete rating",
            description = "Deletes a rating for a caretaker."
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRating(
            @PathVariable Long caretakerId,
            Principal principal
    ) {
        caretakerService.deleteRating(caretakerId, principal.getName());
    }
}
