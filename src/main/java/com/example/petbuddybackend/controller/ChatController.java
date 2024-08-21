package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.paging.PagingParams;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.utils.paging.PagingUtils;
import com.example.petbuddybackend.utils.time.TimeUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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


    @GetMapping("/{chatId}/messages")
    @Operation(
            summary = "Get page of chat messages",
            description =
                    "Retrieves a paginated list of chat messages for a chat room. " +
                    "The page is sorted by message timestamp in descending order (from newest to oldest)."
    )
    @PreAuthorize("isAuthenticated()")
    public Page<ChatMessageDTO> getChatMessages(
            Principal principal,
            @PathVariable Long chatId,
            @Valid @ParameterObject PagingParams pagingParams,
            @Parameter(
                    description = "The time zone to adjust the message timestamps to. If not provided, server's default timezone will be used.",
                    schema = @Schema(type = "string", example = "Europe/Warsaw, CET, +00:02, ...")
            )
            @RequestHeader(value = "${timezone.header-name}", required = false) String acceptTimeZone
    ) {
        Pageable pageable = PagingUtils.createPageable(pagingParams);

        return acceptTimeZone == null ?
                chatService.getChatMessages(chatId, principal.getName(), pageable) :
                chatService.getChatMessages(chatId, principal.getName(), pageable, TimeUtils.getOrSystemDefault(acceptTimeZone));
    }
}
