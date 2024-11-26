package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatRoomDTO;
import com.example.petbuddybackend.entity.chat.ChatMessage;
import com.example.petbuddybackend.entity.user.AppUser;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Mapper(uses = UserMapper.class)
public interface ChatMapper {

    ChatMapper INSTANCE = Mappers.getMapper(ChatMapper.class);

    @Mapping(target = "senderEmail", source = "sender.email")
    @Mapping(target = "chatId", source = "chatRoom.id")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "mapToZonedDateTime")
    ChatMessageDTO mapToChatMessageDTO(ChatMessage chatMessage, @Context ZoneId zoneId);

    @Mapping(target = "senderEmail", source = "sender.email")
    @Mapping(target = "chatId", source = "chatRoom.id")
    ChatMessageDTO mapToChatMessageDTO(ChatMessage chatMessage);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "lastMessage", expression = "java(mapToChatMessageDTO(lastMessage, zoneId))")
    ChatRoomDTO mapToChatRoomDTO(
            Long id,
            AppUser chatter,
            ChatMessage lastMessage,
            @Context ZoneId zoneId
    );

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "mapToZonedDateTime")
    ChatMessageDTO mapTimeZone(ChatMessageDTO chatMessage, @Context ZoneId zoneId);

    @Mapping(target = "lastMessage.createdAt", source = "lastMessage.createdAt", qualifiedByName = "mapToZonedDateTime")
    ChatRoomDTO mapTimeZone(ChatRoomDTO chatRoom, @Context ZoneId zoneId);

    @Named("mapToZonedDateTime")
    default ZonedDateTime mapToZonedDateTime(ZonedDateTime date, @Context ZoneId zoneId) {
        return date.withZoneSameInstant(zoneId);
    }
}
