package com.example.petbuddybackend.service.notification;

import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import com.example.petbuddybackend.entity.notification.ClientNotification;
import com.example.petbuddybackend.entity.notification.ObjectType;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.notification.CaretakerNotificationRepository;
import com.example.petbuddybackend.repository.notification.ClientNotificationRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CaretakerNotificationRepository caretakerNotificationRepository;

    @Autowired
    private ClientNotificationRepository clientNotificationRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CaretakerRepository caretakerRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @SpyBean
    private WebsocketNotificationService websocketNotificationService;

    private Caretaker caretaker;
    private Client client;

    @AfterEach
    void cleanUp() {
        caretakerNotificationRepository.deleteAll();
        clientNotificationRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        caretaker = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository);
        client = PersistenceUtils.addClient(appUserRepository, clientRepository);
    }

    @Test
    @Transactional
    void testAddNotificationForCaretakerAndSend_shouldAddNotificationAndSend() {

        //Given When
        notificationService.addNotificationForCaretakerAndSend(1L, ObjectType.CARE, caretaker,
                "messageKey", Set.of("clientEmail"));

        //Then
        CaretakerNotification notification = caretakerNotificationRepository.findAll().get(0);
        assertNotNull(notification);
        assertEquals(1L, notification.getObjectId());
        assertEquals(ObjectType.CARE, notification.getObjectType());
        assertEquals("messageKey", notification.getMessageKey());
        assertEquals(Set.of("clientEmail"), notification.getArgs());
        assertEquals(caretaker, notification.getCaretaker());
        assertFalse(notification.isRead());

        verify(websocketNotificationService, times(1)).sendNotification(caretaker.getEmail(), notification);

    }

    @Test
    @Transactional
    void testAddNotificationForClientAndSend_shouldAddNotificationAndSend() {

        //Given When
        notificationService.addNotificationForClientAndSend(1L, ObjectType.CARE, client,
                "messageKey", Set.of("caretakerEmail"));

        //Then
        ClientNotification notification = clientNotificationRepository.findAll().get(0);
        assertNotNull(notification);
        assertEquals(1L, notification.getObjectId());
        assertEquals(ObjectType.CARE, notification.getObjectType());
        assertEquals("messageKey", notification.getMessageKey());
        assertEquals(Set.of("caretakerEmail"), notification.getArgs());
        assertEquals(client, notification.getClient());
        assertFalse(notification.isRead());

        verify(websocketNotificationService, times(1)).sendNotification(client.getEmail(), notification);

    }

}
