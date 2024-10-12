package com.example.petbuddybackend.testutils.websocket;

import com.example.petbuddybackend.controller.websocket.ChatWebSocketIntegrationTest;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationConnected;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.testconfig.NoSecurityInjectUserConfig;
import lombok.SneakyThrows;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import static java.util.concurrent.TimeUnit.SECONDS;

public class WebsocketUtils {

    @SneakyThrows
    public static StompSession connectToWebSocket(WebSocketStompClient stompClient, int PORT, String websocketUrlPattern) {
        return stompClient.connectAsync(
                String.format(websocketUrlPattern, PORT),
                new StompSessionHandlerAdapter() {}
        ).get(1, SECONDS);
    }

    public static StompSession.Subscription subscribeToTopic(
            StompSession stompSession,
            ChatNotificationConnected chatMessageConnected,
    ) {
        NoSecurityInjectUserConfig.injectedUsername = chatMessageConnected.getConnectingUserEmail();
        String destinationFormated = String.format(SUBSCRIPTION_URL_PATTERN, 1L, chatMessageConnected.getSessionId());
        StompHeaders headers = createHeaders(destinationFormated, timeZone, role);
        return stompSession.subscribe(headers, new ChatWebSocketIntegrationTest.ChatNotificationFrameHandler());
    }

}
