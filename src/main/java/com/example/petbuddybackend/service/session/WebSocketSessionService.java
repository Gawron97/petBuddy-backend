package com.example.petbuddybackend.service.session;

import com.example.petbuddybackend.utils.time.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class WebSocketSessionService {

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
}
