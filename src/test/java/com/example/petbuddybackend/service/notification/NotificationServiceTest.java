package com.example.petbuddybackend.service.notification;

import com.example.petbuddybackend.dto.notification.SimplyNotificationDTO;
import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import com.example.petbuddybackend.entity.notification.ClientNotification;
import com.example.petbuddybackend.entity.notification.ObjectType;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.repository.notification.CaretakerNotificationRepository;
import com.example.petbuddybackend.repository.notification.ClientNotificationRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import com.example.petbuddybackend.testutils.ReflectionUtils;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private WebsocketNotificationSender websocketNotificationSender;

    @Autowired
    private TransactionTemplate transactionTemplate;

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
        notificationService.addNotificationForCaretakerAndSend(1L, ObjectType.CARE, caretaker, client,
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

        verify(websocketNotificationSender, times(1)).sendNotification(eq(caretaker.getEmail()), any());

    }

    @Test
    @Transactional
    void testAddNotificationForClientAndSend_shouldAddNotificationAndSend() {

        //Given When
        notificationService.addNotificationForClientAndSend(1L, ObjectType.CARE, client, caretaker,
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

        verify(websocketNotificationSender, times(1)).sendNotification(eq(client.getEmail()), any());

    }

    @Test
    void testGetNotifications_sortingParamsShouldAlignWithDTO() {

        List<String> fieldNames = ReflectionUtils.getPrimitiveNames(SimplyNotificationDTO.class);
        fieldNames.remove("notificationId");
        fieldNames.add("id");
        fieldNames.remove("dType");

        for(String fieldName : fieldNames) {
            assertDoesNotThrow(() -> notificationService.getNotifications(
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, fieldName)),
                    "test",
                    Role.CARETAKER,
                    ZoneId.systemDefault()
            ));
        }

    }

    @Test
    void getNotifications_shouldGetAllNotifications() {

        //Given
        transactionTemplate.execute(status -> {
            CaretakerNotification notification1 = PersistenceUtils.addCaretakerNotification(
                    caretakerNotificationRepository, caretaker, client, 1L
            );
            CaretakerNotification notification2 = PersistenceUtils.addCaretakerNotification(
                    caretakerNotificationRepository, caretaker, client, 2L
            );
            CaretakerNotification readNotification = PersistenceUtils.addCaretakerNotification(
                    caretakerNotificationRepository, caretaker, client, 3L
            );
            readNotification.setRead(true);
            return null;
        });

        //When Then
        transactionTemplate.execute(status -> {
            Page<SimplyNotificationDTO> result = notificationService.getNotifications(
                    PageRequest.of(0, 10),
                    caretaker.getEmail(),
                    Role.CARETAKER,
                    ZoneId.systemDefault()
            );
            assertEquals(3, result.getTotalElements());
            return null;
        });

    }

    @Test
    void testMarkNotificationAsRead_shouldMarkNotificationAsRead() {

        //Given
        Long notificationId = transactionTemplate.execute(status -> {
            CaretakerNotification notification = PersistenceUtils.addCaretakerNotification(
                    caretakerNotificationRepository, caretaker, client
            );
            return notification.getId();
        });

        //When
        transactionTemplate.execute(status -> {
            SimplyNotificationDTO result = notificationService.markNotificationAsRead(
                    notificationId,
                    Role.CARETAKER,
                    ZoneId.systemDefault()
            );
            assertEquals(Role.CARETAKER, result.getReceiverProfile());
            return null;
        });

        transactionTemplate.execute(status -> {
            CaretakerNotification notification = caretakerNotificationRepository.findById(notificationId).get();
            assertTrue(notification.isRead());
            return null;
        });
    }

    @Test
    void testMarkNotificationAsRead_shouldThrowExceptionWhenNotificationNotFound() {

        //When Then
        assertThrows(NotFoundException.class,
                () -> notificationService.markNotificationAsRead(
                        1L,
                        Role.CARETAKER,
                        ZoneId.systemDefault()
                )
        );
    }

    @Test
    void testMarkNotificationsAsRead_shouldMarkNotificationsAsRead() {

        //Given
        Long notificationId = transactionTemplate.execute(status -> {
            CaretakerNotification notification = PersistenceUtils.addCaretakerNotification(
                    caretakerNotificationRepository, caretaker, client
            );
            return notification.getId();
        });

        //When
        transactionTemplate.execute(status -> {
            notificationService.markNotificationsAsRead(
                    caretaker.getEmail(),
                    Role.CARETAKER
            );
            return null;
        });

        transactionTemplate.execute(status -> {
            CaretakerNotification notification = caretakerNotificationRepository.findById(notificationId).get();
            assertTrue(notification.isRead());
            return null;
        });
    }

}
