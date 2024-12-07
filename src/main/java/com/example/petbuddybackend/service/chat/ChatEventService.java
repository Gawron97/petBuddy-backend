package com.example.petbuddybackend.service.chat;

import com.example.petbuddybackend.dto.chat.notification.ChatNotificationJoin;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationLeave;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.service.session.WebSocketSessionService;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatEventService {

    @Value("${url.chat.topic.subscribe-prefix}")
    private String CHAT_TOPIC_URL_PREFIX;

    @Value("${url.chat.topic.chat-id-pos}")
    private int CHAT_ID_INDEX_IN_TOPIC_URL;

    private final WebSocketSessionService webSocketSessionService;
    private final ChatService chatService;
    private final WebSocketChatMessageSender webSocketChatMessageSender;

    public void onUserJoinChatRoom(String joiningUsername, Long chatId) {
        ChatRoom chatRoom = chatService.getChatRoomById(chatId);
        Map<Long, Integer> allChatRoomSessions = webSocketSessionService.countChatRoomSessions(joiningUsername);

        if(allChatRoomSessions.containsKey(chatId) && allChatRoomSessions.get(chatId) == 1) {
            log.trace("Sending join message to chat room: {}", chatId);
            chatService.markMessagesAsSeen(chatId, joiningUsername);
            webSocketChatMessageSender.sendMessages(chatRoom, new ChatNotificationJoin(chatId, joiningUsername));
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

    private void onUserUnsubscribe(String leavingUsername, Long chatId, Map<Long, Integer> allChatRoomSessions) {
        int leavingSessionsCount = allChatRoomSessions.getOrDefault(chatId, 0);

        if(leavingSessionsCount == 0) {
            log.trace("Sending leave message to chat room: {}", chatId);
            webSocketChatMessageSender.sendMessages(
                    chatService.getChatRoomById(chatId),
                    new ChatNotificationLeave(chatId, leavingUsername)
            );
        }
    }

    public boolean isRecipientInChat(String recipientUsername, ChatRoom chatRoom) {
        return webSocketSessionService.getUserSubscriptionStartingWithDestination(recipientUsername, CHAT_TOPIC_URL_PREFIX)
                .stream()
                .map(SimpSubscription::getDestination)
                .map(destination -> HeaderUtils.getLongFromDestination(destination, CHAT_ID_INDEX_IN_TOPIC_URL))
                .anyMatch(chatId -> chatId.equals(chatRoom.getId()));
    }
}
