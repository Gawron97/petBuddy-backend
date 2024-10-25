package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.UserProfilesData;
import com.example.petbuddybackend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
            summary = "Get user profile data",
            description = "Returns profile data associated with the principal."
    )
    @GetMapping("/available-profiles")
    @PreAuthorize("isAuthenticated()")
    public UserProfilesData getAvailableUserProfiles(Principal principal) {
        return userService.getProfileData(principal.getName());
    }

    @PostMapping(value = "/profile-picture", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(
            summary = "Upload profile picture",
            description = """
                Uploads profile picture that is shared between all user profiles.
                If user already has a profile picture, it will be replaced.
                
                Maximum file size: ${spring.servlet.multipart.max-file-size}
                """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or image type"),
            @ApiResponse(responseCode = "413", description = "Uploaded file exceeds the maximum allowed size of ${spring.servlet.multipart.max-file-size}")
    })
    @PreAuthorize("isAuthenticated()")
    public AccountDataDTO uploadProfilePicture(
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

    @PostMapping("/block/{username}")
    @Operation(
            summary = "Block user",
            description = "Blocks user with given username."
    )
    @PreAuthorize("isAuthenticated()")
    public void blockUser(Principal principal, @PathVariable String username) {
        userService.blockUser(principal.getName(), username);
    }

    @DeleteMapping("/block/{username}")
    @Operation(
            summary = "Unblock user",
            description = "Unblocks user with given username."
    )
    @PreAuthorize("isAuthenticated()")
    public void unblockUser(Principal principal, @PathVariable String username) {
        userService.unblockUser(principal.getName(), username);
    }
}
