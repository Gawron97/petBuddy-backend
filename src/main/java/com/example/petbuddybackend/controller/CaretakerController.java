package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.paging.PagingParams;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.service.user.CaretakerService;
import com.example.petbuddybackend.utils.paging.PagingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/caretaker")
public class CaretakerController {

    private final CaretakerService caretakerService;

    @GetMapping
    public Page<CaretakerDTO> getCaretakers(
            @ModelAttribute PagingParams pagingParams
    ) {
        var pageable = PagingUtils.createPageable(pagingParams);
        return caretakerService.getCaretakers(pageable);
    }
}

