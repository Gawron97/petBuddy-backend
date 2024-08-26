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

    @PostMapping("/{careId}/caretaker-accept")
    public CareDTO acceptCareByCaretaker(@PathVariable Long careId, Principal principal) {
        return careService.acceptCareByCaretaker(careId, principal.getName());
    }

    @PostMapping("/{careId}/client-accept")
    public CareDTO acceptCareByClient(@PathVariable Long careId, Principal principal) {
        return careService.acceptCareByClient(careId, principal.getName());
    }

    @PostMapping("/{careId}/caretaker-reject")
    public CareDTO rejectCareByCaretaker(@PathVariable Long careId, Principal principal) {
        return careService.rejectCareByCaretaker(careId, principal.getName());
    }

    @PostMapping("/{careId}/client-cancel")
    public CareDTO cancelCareByClient(@PathVariable Long careId, Principal principal) {
        return careService.cancelCareByClient(careId, principal.getName());
    }

}
