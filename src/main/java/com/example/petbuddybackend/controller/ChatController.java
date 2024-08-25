package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatMessageSent;
import com.example.petbuddybackend.dto.paging.PagingParams;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.utils.paging.PagingUtils;
import com.example.petbuddybackend.utils.time.TimeUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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
            @Valid @ParameterObject PagingParams pagingParams,
            @Parameter(
                    description = "The time zone to adjust the message timestamps to. If not provided, server's default timezone will be used.",
                    schema = @Schema(type = "string", example = "Europe/Warsaw, CET, +00:02, ...")
            )
            @RequestHeader(value = "${header-name.timezone}", required = false) String acceptTimeZone
    ) {
        Pageable pageable = PagingUtils.createPageable(pagingParams);

        return acceptTimeZone == null ?
                chatService.getChatMessages(chatId, principal.getName(), pageable) :
                chatService.getChatMessages(chatId, principal.getName(), pageable, TimeUtils.getOrSystemDefault(acceptTimeZone));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{messageReceiverEmail}")
    @Operation(
            summary = "Send chat message and initialize chat room with given user",
            description =
                        """
                        Creates a new chat room with the given user and sends the first message to him.
                        This is the first step of the communication between a Caretaker and a Client.
                        After this step, websocket endpoint should be used instead of this endpoint.
                        
                        To connect to websocket endpoint, use the following path:
                        `/ws`
                        
                        To subscribe to chat room, use the following path:
                        `/topic/messages/{chatId}`
                        
                        To send a message to chat room, use the following path:
                        `/app/chat/{chatId}`
                        
                        Same payload and headers apply to websocket endpoint as to this endpoint.
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
            @RequestHeader(value = "${header-name.timezone}", required = false) String acceptTimeZone,
            @RequestHeader(value = "${header-name.role}") Role acceptRole
    ) {
        return acceptRole == null ?
                chatService.createChatRoomWithMessage(
                        messageReceiverEmail, principal.getName(), acceptRole, message
                ) :
                chatService.createChatRoomWithMessage(
                        messageReceiverEmail, principal.getName(), acceptRole, message, TimeUtils.getOrSystemDefault(acceptTimeZone)
                );
    }
}
