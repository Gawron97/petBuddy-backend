package com.example.petbuddybackend.service.chat;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.notification.*;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.service.mapper.ChatMapper;
import com.example.petbuddybackend.service.session.WebSocketSessionService;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketChatMessageSender {

    @Value("${url.chat.topic.send-url}")
    private String CHAT_TOPIC_URL_PATTERN;

    @Value("${url.chat.topic.subscribe-prefix}")
    private String CHAT_TOPIC_URL_PREFIX;

    @Value("${url.chat.topic.chat-id-pos}")
    private int CHAT_ID_INDEX_IN_TOPIC_URL;

    private final ChatMapper chatMapper = ChatMapper.INSTANCE;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final WebSocketSessionService webSocketSessionService;
    private final ChatService chatService;

    public void sendMessages(ChatRoom chatRoom, ChatNotification notification) {
        Long chatId = chatRoom.getId();
        String clientEmail = chatRoom.getClient().getEmail();
        String caretakerEmail = chatRoom.getCaretaker().getEmail();

        webSocketSessionService.getUserSessions(clientEmail)
                .forEach((simpSession) -> sendToUser(chatId, clientEmail, simpSession.getId(), notification));

        webSocketSessionService.getUserSessions(caretakerEmail)
                .forEach((simpSession) -> sendToUser(chatId, caretakerEmail, simpSession.getId(), notification));
    }

    /**
     * Sends block message to chat room of users. Users can have two chats but as different role for example:
     * user1: client - user2: caretaker
     * and other chat
     * user1: caretaker - user2: client
     * */
    public void sendBlockMessageToUsers(String firstUsername, String secondUsername, BlockType blockType) {
        chatService.findChatRoomByParticipants(firstUsername, secondUsername)
                .ifPresent(chatRoom -> sendMessages(
                        chatRoom,
                        new ChatNotificationBlock(chatRoom.getId(), blockType))
                );

        chatService.findChatRoomByParticipants(secondUsername, firstUsername)
                .ifPresent(chatRoom -> sendMessages(
                        chatRoom,
                        new ChatNotificationBlock(chatRoom.getId(), blockType))
                );
    }

    public void onUserJoinChatRoom(String joiningUsername, Long chatId) {
        ChatRoom chatRoom = chatService.getChatRoomById(chatId);
        Map<Long, Integer> allChatRoomSessions = webSocketSessionService.countChatRoomSessions(joiningUsername);

        if(allChatRoomSessions.containsKey(chatId) && allChatRoomSessions.get(chatId) == 1) {
            log.trace("Sending join message to chat room: {}", chatId);
            chatService.markMessagesAsSeen(chatId, joiningUsername);
            sendMessages(chatRoom, new ChatNotificationJoin(chatId, joiningUsername));
        }
    }

    public void onUserDisconnect(String leavingUsername, Map<String, Long> subscriptions) {
        Map<Long, Integer> allChatRoomSessions = webSocketSessionService.countChatRoomSessions(leavingUsername);

        for(Long chatId : subscriptions.values()) {
            onUserUnsubscribe(leavingUsername, chatId, allChatRoomSessions);
        }
    }

    public void onUserUnsubscribe(String leavingUsername, Long chatId) {
        Map<Long, Integer> allChatRoomSessions = webSocketSessionService.countChatRoomSessions(leavingUsername);
        onUserUnsubscribe(leavingUsername, chatId, allChatRoomSessions);
    }

    public boolean isRecipientInChat(String recipientUsername, ChatRoom chatRoom) {
        return webSocketSessionService.getUserSubscriptionStartingWithDestination(recipientUsername, CHAT_TOPIC_URL_PREFIX)
                .stream()
                .map(SimpSubscription::getDestination)
                .map(destination -> HeaderUtils.getLongFromDestination(destination, CHAT_ID_INDEX_IN_TOPIC_URL))
                .anyMatch(chatId -> chatId.equals(chatRoom.getId()));
    }

    private void onUserUnsubscribe(String leavingUsername, Long chatId, Map<Long, Integer> allChatRoomSessions) {
        int leavingSessionsCount = allChatRoomSessions.getOrDefault(chatId, 0);

        if(leavingSessionsCount == 0) {
            log.trace("Sending leave message to chat room: {}", chatId);
            sendMessages(
                    chatService.getChatRoomById(chatId),
                    new ChatNotificationLeave(chatId, leavingUsername)
            );
        }
    }

    private void sendToUser(Long chatId, String receiverUsername, String sessionId, ChatNotification notification) {
        if(notification instanceof ChatNotificationSend messageNotification) {
            log.trace("Sending message notification to user: {}, session {}", receiverUsername, sessionId);
            notifyWithMessage(chatId, receiverUsername, sessionId, messageNotification);
        } else {
            log.trace("Sending generic notification to user: {}, session {}", receiverUsername, sessionId);
            notifyWithGenericMessage(chatId, receiverUsername, sessionId, notification);
        }
    }

    private void notifyWithMessage(
            Long chatId,
            String receiverUsername,
            String sessionId,
            ChatNotificationSend message
    ) {
        ZoneId zoneId = webSocketSessionService.getTimezoneOrDefault(sessionId);
        convertNotificationMessageTimezone(message, zoneId);
        notifyWithGenericMessage(chatId, receiverUsername, sessionId, message);
    }

    private void notifyWithGenericMessage(
            Long chatId,
            String username,
            String sessionId,
            ChatNotification notification
    ) {
        simpMessagingTemplate.convertAndSendToUser(
                username,
                String.format(CHAT_TOPIC_URL_PATTERN, chatId),
                notification,
                HeaderUtils.createMessageHeadersWithSessionId(sessionId)
        );
    }

    private void convertNotificationMessageTimezone(ChatNotification notification, ZoneId zoneId) {
        ChatNotificationSend notificationMessage = (ChatNotificationSend) notification;
        ChatMessageDTO mappedMessage = chatMapper.mapTimeZone(notificationMessage.getContent(), zoneId);
        notificationMessage.setContent(mappedMessage);
    }
}
