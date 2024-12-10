package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.UserProfilesData;
import com.example.petbuddybackend.service.block.BlockService;
import com.example.petbuddybackend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final BlockService blockService;

    @Operation(
            summary = "Get user profile data",
            description = "Returns profile data associated with the principal."
    )
    @GetMapping("/available-profiles")
    @PreAuthorize("isAuthenticated()")
    public UserProfilesData getAvailableUserProfiles(Principal principal) {
        return userService.getProfileData(principal.getName());
    }

    @PutMapping(value = "/profile-picture", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
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

    @GetMapping("/block")
    @Operation(
            summary = "Get users blocked by currently logged in user",
            description = "Returns users blocked by the principal sorted by blocked user email."
    )
    @PreAuthorize("isAuthenticated()")
    public List<AccountDataDTO> getUsersBlockedByUser(Principal principal) {
        return blockService.getUsersBlockedByUserSortedByBlockedUsername(principal.getName());
    }

    @PostMapping("/block/{username}")
    @Operation(
            summary = "Block user",
            description = """
                        # Description
                        Blocks user with given username. Users blocked by the principal will not be able to interact
                        with each other. They will not be able to send messages and create reservations for cares.
                        
                        # Care state change
                        All cares between users **will be cancelled** if the state permits.
                        """
    )
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void blockUser(Principal principal, @PathVariable String username) {
        blockService.blockUser(principal.getName(), username);
    }

    @DeleteMapping("/block/{username}")
    @Operation(
            summary = "Unblock user",
            description = "Unblocks user with given username."
    )
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unblockUser(Principal principal, @PathVariable String username) {
        blockService.unblockUser(principal.getName(), username);
    }
}
