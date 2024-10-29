package com.example.petbuddybackend.service.chat.session;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.notification.ChatNotification;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationJoined;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationLeft;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationMessage;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.service.mapper.ChatMapper;
import com.example.petbuddybackend.service.session.WebSocketSessionService;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionService {

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
    private final ChatSessionTracker chatSessionTracker;

    public void sendMessages(ChatRoom chatRoom, ChatNotification notification) {
        Long chatId = chatRoom.getId();
        String clientEmail = chatRoom.getClient().getEmail();
        String caretakerEmail = chatRoom.getCaretaker().getEmail();

        webSocketSessionService.getUserSessions(clientEmail)
                .forEach((simpSession) -> sendToUser(chatId, clientEmail, simpSession.getId(), notification));

        webSocketSessionService.getUserSessions(caretakerEmail)
                .forEach((simpSession) -> sendToUser(chatId, caretakerEmail, simpSession.getId(), notification));
    }

    public void onUserJoinChatRoom(String joiningUsername, Long chatId, String subId) {
        chatSessionTracker.addSubscription(subId, chatId);
        Map<Long, Integer> allChatRoomSessions = countChatRoomSessions(joiningUsername);

        if(allChatRoomSessions.containsKey(chatId) && allChatRoomSessions.get(chatId) == 1) {
            log.trace("Sending join message to chat room: {}", chatId);
            chatService.updateLastMessageSeen(chatId, joiningUsername);
            sendMessages(
                    chatService.getChatRoomById(chatId),
                    new ChatNotificationJoined(chatId, joiningUsername)
            );
        }
    }

    public void onUserDisconnect(String leavingUsername) {
        Set<String> subIds = chatSessionTracker.getSubscriptionIds();
        Map<Long, Integer> allChatRoomSessions = countChatRoomSessions(leavingUsername);

        for(String subId : subIds) {
            onUserUnsubscribe(leavingUsername, subId, allChatRoomSessions);
        }
    }

    public void onUserUnsubscribe(String leavingUsername, String subId) {
        Map<Long, Integer> allChatRoomSessions = countChatRoomSessions(leavingUsername);
        onUserUnsubscribe(leavingUsername, subId, allChatRoomSessions);
    }

    public boolean isRecipientInChat(String senderUsername, ChatRoom chatRoom) {
        String recipientUsername = chatRoom.getClient().getEmail().equals(senderUsername) ?
                chatRoom.getCaretaker().getEmail() :
                chatRoom.getClient().getEmail();

        Set<SimpSession> recipientSessions = webSocketSessionService.getUserSessions(recipientUsername);

        for(SimpSession session : recipientSessions) {
            Set<SimpSubscription> subs = session.getSubscriptions();
            for(SimpSubscription sub : subs) {
                if(HeaderUtils.destinationStartsWith(CHAT_TOPIC_URL_PREFIX, sub.getDestination())) {
                    Long chatId = HeaderUtils.getLongFromDestination(sub.getDestination(), CHAT_ID_INDEX_IN_TOPIC_URL);
                    if(chatId.equals(chatRoom.getId())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void onUserUnsubscribe(String leavingUsername, String subId, Map<Long, Integer> allChatRoomSessions) {
        Long chatId = chatSessionTracker.getChatId(subId);
        int leavingSessionsCount = allChatRoomSessions.getOrDefault(chatId, 0);

        if(leavingSessionsCount == 0) {
            log.trace("Sending leave message to chat room: {}", chatId);
            sendMessages(
                    chatService.getChatRoomById(chatId),
                    new ChatNotificationLeft(chatId, leavingUsername)
            );
        }

        chatSessionTracker.removeSubscription(subId);
    }

    /**
     * @return Map with chatId as key and number of sessions as value
     * */
    private Map<Long, Integer> countChatRoomSessions(String username) {
        Set<SimpSession> userSessions = webSocketSessionService.getUserSessions(username);
        Map<Long, Integer> chatRoomSessions = new HashMap<>();

        for(SimpSession session : userSessions) {
            Set<SimpSubscription> subs = session.getSubscriptions();
            countSessionsFromSubs(subs, chatRoomSessions);
        }

        return chatRoomSessions;
    }

    /**
     * @return Map with chatId as key and number of sessions as value
     * */
    private Map<Long, Integer> countSessionsFromSubs(Set<SimpSubscription> subs, Map<Long, Integer> buffer) {
        for(SimpSubscription sub : subs) {
            if(HeaderUtils.destinationStartsWith(CHAT_TOPIC_URL_PREFIX, sub.getDestination())) {
                Long chatId = HeaderUtils.getLongFromDestination(sub.getDestination(), CHAT_ID_INDEX_IN_TOPIC_URL);
                buffer.put(chatId, buffer.getOrDefault(chatId, 0) + 1);
            }
        }

        return buffer;
    }

    private void sendToUser(Long chatId, String receiverUsername, String sessionId, ChatNotification notification) {
        if(notification instanceof ChatNotificationMessage messageNotification) {
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
            ChatNotificationMessage message
    ) {
        ZoneId zoneId = webSocketSessionService.getTimezoneOrDefault(sessionId);
        convertNotificationMessageTimezone(message, zoneId);

        notifyWithGenericMessage(chatId, receiverUsername, sessionId, message);

        if(!message.getContent().getSenderEmail().equals(receiverUsername)) {
            chatService.updateLastMessageSeen(chatId, message.getContent().getSenderEmail());
        }
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
        ChatNotificationMessage notificationMessage = (ChatNotificationMessage) notification;
        ChatMessageDTO mappedMessage = chatMapper.mapTimeZone(notificationMessage.getContent(), zoneId);
        notificationMessage.setContent(mappedMessage);
    }
}
