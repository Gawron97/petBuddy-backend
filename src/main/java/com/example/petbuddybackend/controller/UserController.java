package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.photo.PhotoLinkDTO;
import com.example.petbuddybackend.dto.user.UserProfiles;
import com.example.petbuddybackend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get available user profiles")
    @GetMapping("/available-profiles")
    @PreAuthorize("isAuthenticated()")
    public UserProfiles getAvailableUserProfiles(Principal principal) {
        return userService.getUserProfiles(principal.getName());
    }

    @PostMapping("/profile-picture")
    @Operation(
            summary = "Upload profile picture",
            description = "Uploads profile picture that is shared between all user profiles"
    )
    @PreAuthorize("isAuthenticated()")
    public PhotoLinkDTO uploadProfilePicture(
            Principal principal,
            MultipartFile profilePicture
    ) {
        return userService.uploadProfilePicture(principal.getName(), profilePicture);
    }

    @DeleteMapping("/profile-picture")
    @Operation(
            summary = "Delete profile picture",
            description = "Deletes profile picture that is shared between all user profiles"
    )
    @PreAuthorize("isAuthenticated()")
    public void deleteProfilePicture(Principal principal) {
        userService.deleteProfilePicture(principal.getName());
    }
}
