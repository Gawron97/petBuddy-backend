package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.photo.PhotoLinkDTO;
import com.example.petbuddybackend.dto.user.ProfileData;
import com.example.petbuddybackend.dto.user.UserProfiles;
import com.example.petbuddybackend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get available user profiles")
    @GetMapping("/available-profiles")
    @PreAuthorize("isAuthenticated()")
    public UserProfiles getAvailableUserProfiles(Principal principal) {
        return userService.getUserProfiles(principal.getName());
    }

    @GetMapping
    @Operation(
            summary = "Get user profile data",
            description = "Returns profile data associated with the principal."
    )
    @PreAuthorize("isAuthenticated()")
    public ProfileData getUserProfileData(Principal principal) {
        return userService.getProfileData(principal.getName());
    }

    @PostMapping(value = "/profile-picture", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(
            summary = "Upload profile picture",
            description = """
                Uploads profile picture that is shared between all user profiles.
                If user already has a profile picture, it will be replaced.
                """
    )
    @PreAuthorize("isAuthenticated()")
    public PhotoLinkDTO uploadProfilePicture(
            Principal principal,
            @RequestPart MultipartFile profilePicture
    ) {
        return userService.uploadProfilePicture(principal.getName(), profilePicture);
    }

    @DeleteMapping("/profile-picture")
    @Operation(
            summary = "Delete profile picture",
            description = "Deletes profile picture. All user profiles will be affected."
    )
    @PreAuthorize("isAuthenticated()")
    public void deleteProfilePicture(Principal principal) {
        userService.deleteProfilePicture(principal.getName());
    }
}
