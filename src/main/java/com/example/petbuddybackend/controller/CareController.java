package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.care.CareDTO;
import com.example.petbuddybackend.dto.care.CreateCareDTO;
import com.example.petbuddybackend.dto.care.UpdateCareDTO;
import com.example.petbuddybackend.service.care.CareService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/care")
@RequiredArgsConstructor
public class CareController {

    private final CareService careService;

    @PostMapping("/reservation")
    public CareDTO makeReservation(@RequestBody CreateCareDTO createCare, Principal principal) {
        return careService.makeReservation(createCare, principal.getName());
    }

    @PatchMapping("/{careId}/update")
    public CareDTO updateCare(@PathVariable Long careId, @RequestBody UpdateCareDTO updateCare, Principal principal) {
        return careService.updateCare(careId, updateCare, principal.getName());
    }

}
