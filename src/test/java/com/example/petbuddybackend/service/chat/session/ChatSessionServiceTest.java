package com.example.petbuddybackend.service.chat.session;

import com.example.petbuddybackend.service.mapper.ChatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@SpringBootTest
public class ChatSessionServiceTest {

    @Value("${url.chat.topic.client-subscribe-pattern}")
    private String SUBSCRIPTION_URL_PATTERN;

    @Autowired
    private ChatSessionService chatSessionService;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    private ChatMapper chatMapper = ChatMapper.INSTANCE;


}
