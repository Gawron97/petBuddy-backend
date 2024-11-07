package com.example.petbuddybackend.service.session;

import com.example.petbuddybackend.utils.header.HeaderUtils;
import com.example.petbuddybackend.utils.time.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WebSocketSessionService {

    @Value("${url.chat.topic.subscribe-prefix}")
    private String CHAT_TOPIC_URL_PREFIX;

    @Value("${url.chat.topic.chat-id-pos}")
    private int CHAT_ID_INDEX_IN_TOPIC_URL;

    private final SimpUserRegistry simpUserRegistry;
    private final Map<String, ZoneId> sessionsTimeZone = new ConcurrentHashMap<>();

    public Integer getNumberOfSessions(String userEmail) {
        return getUserSessions(userEmail).size();
    }

    public void storeUserTimeZoneWithSession(String sessionId, String zoneId) {
        sessionsTimeZone.put(sessionId, TimeUtils.getOrSystemDefault(zoneId));
    }

    public void removeUserSessionWithTimeZone(String sessionId) {
        sessionsTimeZone.remove(sessionId);
    }

    public boolean isUserConnected(String userEmail) {
        SimpUser simpUser = simpUserRegistry.getUser(userEmail);
        return simpUser != null && !simpUser.getSessions().isEmpty();
    }

    public Set<SimpSession> getUserSessions(String userEmail) {
        SimpUser simpUser = simpUserRegistry.getUser(userEmail);
        if(simpUser == null) {
            return Collections.emptySet();
        }
        return simpUser.getSessions();
    }

    public ZoneId getTimezoneOrDefault(SimpSession session) {
        return sessionsTimeZone.getOrDefault(session.getId(), ZoneId.systemDefault());
    }

    public ZoneId getTimezoneOrDefault(String sessionId) {
        return sessionsTimeZone.getOrDefault(sessionId, ZoneId.systemDefault());
    }

    public Set<SimpSubscription> getUserSubscriptionStartingWithDestination(String username, String destinationPrefix) {
        Set<SimpSession> recipientSessions = getUserSessions(username);

        return recipientSessions.stream()
                .map(SimpSession::getSubscriptions)
                .flatMap(Set::stream)
                .filter(sub -> HeaderUtils.destinationStartsWith(destinationPrefix, sub.getDestination()))
                .collect(Collectors.toSet());
    }

    /**
     * @return Map with chatId as key and number of sessions as value
     * */
    public Map<Long, Integer> countChatRoomSessions(String username) {
        Set<SimpSession> userSessions = getUserSessions(username);
        Map<Long, Integer> chatRoomSessions = new HashMap<>();

        for(SimpSession session : userSessions) {
            Set<SimpSubscription> subs = session.getSubscriptions();
            for(SimpSubscription sub : subs) {
                if(HeaderUtils.destinationStartsWith(CHAT_TOPIC_URL_PREFIX, sub.getDestination())) {
                    Long chatId = HeaderUtils.getLongFromDestination(sub.getDestination(), CHAT_ID_INDEX_IN_TOPIC_URL);
                    chatRoomSessions.put(chatId, chatRoomSessions.getOrDefault(chatId, 0) + 1);
                }
            }
        }

        return chatRoomSessions;
    }
}
