package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.chat.ChatRoomDTO;
import com.example.petbuddybackend.utils.swaggerdocs.RoleParameter;
import com.example.petbuddybackend.utils.swaggerdocs.TimeZoneParameter;
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
import java.util.Optional;

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
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) Optional<String>  acceptTimeZone
    ) {
        Pageable pageable = PagingUtils.createPageable(pagingParams);

        return acceptTimeZone.isPresent() ?
                chatService.getChatMessages(
                        chatId,
                        principal.getName(),
                        pageable,
                        TimeUtils.getOrSystemDefault(acceptTimeZone.get())
                ) :
                chatService.getChatMessages(
                        chatId,
                        principal.getName(),
                        pageable
                );
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{messageReceiverEmail}")
    @Operation(
            summary = "Send chat message and initialize chat room with given user",
            description =
                    """
                    ## Endpoint description
                    Creates a new chat room with the given user and sends the first message to him. This is the 
                    first step of the communication between a Caretaker and a Client. After this step, websocket 
                    endpoint should be used instead of this endpoint.
                    
                    **Role header** determines the sender's role in the chat room. If the principal is, for example, 
                    a Caretaker, the receiver is assumed to be a Client.
                    
                    ## Connecting to websocket endpoint
                    - To connect to websocket endpoint, use the following path: `/ws`
                    - To subscribe to chat room, use the following path: `/topic/messages/{chatId}`
                    - To send a message to chat room, use the following path: `/app/chat/{chatId}`
                    
                    Same payload and headers apply to websocket endpoint as to this endpoint,
                    except the time zone related header as it will be added in the future PR.
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
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) Optional<String> acceptTimeZone,
            @RoleParameter @RequestHeader(value = "${header-name.role}") Role acceptRole
    ) {
        return acceptTimeZone.isPresent() ?
                chatService.createChatRoomWithMessage(
                        messageReceiverEmail,
                        principal.getName(),
                        acceptRole,
                        message,
                        TimeUtils.getOrSystemDefault(acceptTimeZone.get())
                ) :
                chatService.createChatRoomWithMessage(
                        messageReceiverEmail,
                        principal.getName(),
                        acceptRole,
                        message
                );
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
            summary = "Get all chat rooms",
            description = """
                          ## Endpoint description
                          Retrieves a list of all chat rooms for the current user. The returned page is sorted by
                          the last message timestamp in descending order (from newest to oldest). The chatRoomId can be 
                          used to connect to the websocket endpoint and send messages to the chat room.
                          
                          **Role header** determines the sender's role in the chat room. If the principal is, for example, 
                          a Caretaker, the receiver is assumed to be a Client.
                          
                          ## Connecting to websocket endpoint
                          - To connect to websocket endpoint, use the following path: `/ws`
                          - To subscribe to chat room, use the following path: `/topic/messages/{chatId}`
                          - To send a message to chat room, use the following path: `/app/chat/{chatId}`
                          
                          Same payload and headers apply to websocket endpoint as to `/api/chat/{messageReceiverEmail}` endpoint,
                          except the time zone related header as it will be added in the future PR.
                          """
    )
    public Page<ChatRoomDTO> getChatRooms(
            Principal principal,
            @RoleParameter @RequestHeader(value = "${header-name.role}") Role acceptRole,
            @TimeZoneParameter @RequestHeader(value = "${header-name.timezone}", required = false) Optional<String> acceptTimeZone,
            @Valid @ParameterObject PagingParams pagingParams
    ) {
        Pageable pageable = PagingUtils.createPageable(pagingParams);

        return acceptTimeZone.isPresent() ?
                chatService.getChatRoomsByParticipantEmail(
                        principal.getName(),
                        acceptRole,
                        pageable,
                        TimeUtils.getOrSystemDefault(acceptTimeZone.get())
                ) :
                chatService.getChatRoomsByParticipantEmail(
                        principal.getName(),
                        acceptRole,
                        pageable
                );
    }
}
