package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.offer.OfferDTO;
import com.example.petbuddybackend.service.offer.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/caretaker/offer")
public class OfferController {

    private final OfferService offerService;

    @PostMapping("/add")
    public ResponseEntity<?> addOffer(@RequestBody OfferDTO offer, Principal principal) {
        return ResponseEntity.ok(offerService.addOrEditOffer(offer, principal.getName()));
    }

}
