package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.notification.SimplyNotificationDTO;
import com.example.petbuddybackend.dto.paging.SortedPagingParams;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.notification.NotificationService;
import com.example.petbuddybackend.utils.annotation.swaggerdocs.RoleParameter;
import com.example.petbuddybackend.utils.annotation.swaggerdocs.TimeZoneParameter;
import com.example.petbuddybackend.utils.paging.PagingUtils;
import com.example.petbuddybackend.utils.time.TimeUtils;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;


    @Operation(
            summary = "Get unread notifications",
            description =
                    """
                            ## Endpoint description
                            Get unread notifications for the current logged user for current profile.
                                                        
                            ## Docs for websocket notifications:
                                                        
                            Available translations:
                            - care_reservation = when user making a reservation
                            - care_update = when user updating price in reservation
                            - care_accepted = when care is accepted
                            - care_rejected = when care is rejected
                                                        
                            Note that notifications about unread chats has different structure.
                            Available dType for notifications:
                            - SIMPLE_NOTIFICATION
                            - CHAT_NOTIFICATION
                            """
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<SimplyNotificationDTO> getUnreadNotifications(
            @ParameterObject @ModelAttribute @Valid SortedPagingParams pagingParams,
            Principal principal,
            @RoleParameter
            @RequestHeader(value = "${header-name.role}") Role role,

            @TimeZoneParameter
            @RequestHeader(value = "${header-name.timezone}", required = false) String timezone) {
        Pageable pageable = PagingUtils.createSortedPageable(pagingParams);
        return notificationService.getUnreadNotifications(
                pageable,
                principal.getName(),
                role,
                TimeUtils.getOrSystemDefault(timezone)
        );
    }

    @Operation(summary = "Mark notification as read")
    @PatchMapping("/{notificationId}")
    @PreAuthorize("isAuthenticated()")
    public SimplyNotificationDTO markNotificationAsRead(@PathVariable Long notificationId,
                                                        @RoleParameter
                                                  @RequestHeader(value = "${header-name.role}") Role role,
                                                        @TimeZoneParameter
                                                  @RequestHeader(value = "${header-name.timezone}") String timezone) {
        return notificationService.markNotificationAsRead(notificationId, role, TimeUtils.getOrSystemDefault(timezone));
    }

}
