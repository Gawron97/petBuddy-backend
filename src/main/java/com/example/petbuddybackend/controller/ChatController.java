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
                    ## Endpoint description
                    Creates a new chat room with the given user and sends the first message to him. This is the 
                    first step of the communication between a Caretaker and a Client. After this step, websocket 
                    endpoint should be used instead of this endpoint.
                    
                    ## Connecting to websocket endpoint
                    - To connect to websocket endpoint, use the following path: `/ws`
                    - To subscribe to chat room, use the following path: `/topic/messages/{chatId}`
                    - To send a message to chat room, use the following path: `/app/chat/{chatId}`
                    
                    ## Payload and headers
                    **Role header** determines the sender's role in the chat room. If the principal is, for example, 
                    a Caretaker, the receiver is assumed to be a Client.
                    
                    **Time zone** from header is cached per session. It is highly recommended to provide the timezone 
                    header when subscribing to the topic. The header does not have to be provided on each message send, 
                    but if it is provided, then the new timezone will be cached.
                    
                    The payload is describing the events happening in the chat room. It has a field `type` that determines
                    the type of the event.
                    
                    ## Chat message types
                    - `MESSAGE` - Used for sending messages. Has field `content` with **ChatMessageDTO**.
                    - `JOINED` - Used for notifying that user joined the chat room. Has fields `chatId` and `joiningUserEmail`.
                    - `LEFT` - Used for notifying that user left the chat room. Has fields `chatId` and `leavingUserEmail`.
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
    @GetMapping
    @Operation(
            summary = "Get all chat rooms",
            description = """
                        ## Endpoint description
                        Creates a new chat room with the given user and sends the first message to him. This is the 
                        first step of the communication between a Caretaker and a Client. After this step, websocket 
                        endpoint should be used instead of this endpoint.
                        
                        ## Connecting to websocket endpoint
                        - To connect to websocket endpoint, use the following path: `/ws`
                        - To subscribe to chat room, use the following path: `/topic/messages/{chatId}`
                        - To send a message to chat room, use the following path: `/app/chat/{chatId}`
                        
                        ## Payload and headers
                        **Role header** determines the sender's role in the chat room. If the principal is, for example, 
                        a Caretaker, the receiver is assumed to be a Client.
                        
                        **Time zone** from header is cached per session. It is highly recommended to provide the timezone 
                        header when subscribing to the topic. The header does not have to be provided on each message send, 
                        but if it is provided, then the new timezone will be cached.
                        
                        The payload is describing the events happening in the chat room. It has a field `type` that determines
                        the type of the event.
                        
                        ## Chat message types
                        - `MESSAGE` - Used for sending messages. Has field `content` with **ChatMessageDTO**.
                        - `JOINED` - Used for notifying that user joined the chat room. Has fields `chatId` and `joiningUserEmail`.
                        - `LEFT` - Used for notifying that user left the chat room. Has fields `chatId` and `leavingUserEmail`.
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
}
