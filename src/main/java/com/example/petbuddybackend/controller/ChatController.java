package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.chat.ChatRoomDTO;
import com.example.petbuddybackend.utils.annotation.swaggerdocs.RoleParameter;
import com.example.petbuddybackend.utils.annotation.swaggerdocs.TimeZoneParameter;
import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatMessageSent;
import com.example.petbuddybackend.dto.paging.PagingParams;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.utils.paging.PagingUtils;
import com.example.petbuddybackend.utils.time.TimeUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{chatId}/messages")
    @Operation(
            summary = "Get page of chat messages",
            description =
                    "Retrieves a paginated list of chat messages for a chat room. " +
                    "The page is sorted by message timestamp in descending order (from newest to oldest)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved chat messages"),
            @ApiResponse(responseCode = "404", description = "Chat room not found"),
            @ApiResponse(responseCode = "403", description = "User is not a participant in the chat room")
    })
    public Page<ChatMessageDTO> getChatMessages(
            Principal principal,
            @PathVariable Long chatId,
            @Valid @ModelAttribute @ParameterObject PagingParams pagingParams,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String acceptTimeZone
    ) {
        Pageable pageable = PagingUtils.createPageable(pagingParams);
        return chatService.getChatMessagesByParticipantEmail(
                chatId,
                principal.getName(),
                pageable,
                TimeUtils.getOrSystemDefault(acceptTimeZone)
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{messageReceiverEmail}")
    @Operation(
            summary = "Send chat message and initialize chat room with given user",
            description =
                    """
                    Creates a new chat room with the given user and sends the first message to him. This is the
                    first step of the communication between a Caretaker and a Client. After this step, websocket 
                    endpoint should be used instead of this endpoint.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully send chat message and created a chat room"),
            @ApiResponse(responseCode = "409", description = "Chat with given person already exists"),
            @ApiResponse(responseCode = "404", description = "Message receiver not found")
    })
    public ChatMessageDTO sendFirstMessage(
            @PathVariable String messageReceiverEmail,
            Principal principal,
            @RequestBody @Valid ChatMessageSent message,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String acceptTimeZone,
            @RoleParameter @RequestHeader(value = "${header-name.role}") Role acceptRole
    ) {
        return chatService.createChatRoomWithMessage(
                principal.getName(),
                acceptRole,
                messageReceiverEmail,
                message,
                TimeUtils.getOrSystemDefault(acceptTimeZone)
        );
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{participantEmail}")
    @Operation(
            summary = "Get chat room with given user",
            description =
                    """
                    Retrieves a chat room with the given user and based on the user role.
                    For example, if header role specifies a Caretaker, the API will look for chat room where
                    principal is a Caretaker and the chatter is a Client.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved chat room"),
            @ApiResponse(responseCode = "404", description = "Message receiver not found")
    })
    public ChatRoomDTO getChatRoom(
            Principal principal,
            @PathVariable String participantEmail,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String acceptTimeZone,
            @RoleParameter @RequestHeader(value = "${header-name.role}") Role acceptRole
    ) {
        return chatService.getChatRoomWithParticipant(
                principal.getName(),
                acceptRole,
                participantEmail,
                TimeUtils.getOrSystemDefault(acceptTimeZone)
        );
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
            summary = "Get all chat rooms",
            description = """
                        Retrieves a paginated list of chat rooms where the user is a participant. Useful for connecting
                        to existing chat room via websocket.
                        """
    )
    public Page<ChatRoomDTO> getChatRooms(
            Principal principal,
            @Valid @ModelAttribute @ParameterObject PagingParams pagingParams,
            @RoleParameter @RequestHeader(value = "${header-name.role}") Role acceptRole,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) String acceptTimeZone
    ) {
        Pageable pageable = PagingUtils.createPageable(pagingParams);
        return chatService.getChatRoomsByParticipantEmail(
                principal.getName(),
                acceptRole,
                pageable,
                TimeUtils.getOrSystemDefault(acceptTimeZone)
        );
    }

    @GetMapping("/unread-number")
    @Operation(
            summary = "Get number of unread chats of currently logged in user"
    )
    @PreAuthorize("isAuthenticated()")
    public Integer getUnreadChatsNumber(Principal principal) {
        return chatService.getUnreadChatsNumber(principal.getName());
    }

}
