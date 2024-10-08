package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.ClientDTO;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.petbuddybackend.testutils.mock.GeneralMockProvider.createJwtToken;
import static com.example.petbuddybackend.testutils.mock.MockUserProvider.createMockCaretaker;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ClientServiceTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CaretakerRepository caretakerRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    private UserService userService;


    @AfterEach
    void tearDown() {
        appUserRepository.deleteAll();
    }

    @Test
    @Transactional
    void createClientIfNotExist_WhenClientDoesNotExist_ThenShouldCreateClient() {
        //Given

        JwtAuthenticationToken token = createJwtToken("test@mail", "firstname", "lastname", "test@mail");

        //When
        clientService.createClientIfNotExist(token);

        //Then
        Client createdClient = clientRepository.findById("test@mail").orElse(null);
        assertNotNull(createdClient);
        assertEquals("test@mail", createdClient.getEmail());
        assertEquals("firstname", createdClient.getAccountData().getName());
        assertEquals("lastname", createdClient.getAccountData().getSurname());
    }

    @Test
    @Transactional
    void createClientIfNotExists_WhenClientExists_ThenShouldNotCreateClient() {
        //Given
        Client client = PersistenceUtils.addClient(appUserRepository, clientRepository);

        JwtAuthenticationToken token = createJwtToken(client.getEmail(), client.getAccountData().getName(),
                client.getAccountData().getSurname(), client.getEmail());

        //When
        clientService.createClientIfNotExist(token);

        //Then
        assertEquals(1, clientRepository.count());
    }

    @Test
    void getClient_ShouldReturnProperClient() {

        //Given
        Client client = PersistenceUtils.addClient(appUserRepository, clientRepository);

        //When
        ClientDTO result = clientService.getClient(client.getEmail());

        //Then
        assertNotNull(result);
        assertEquals(client.getEmail(), result.accountData().email());

    }

    @Test
    void getClient_whenClientNotExist_ShouldThrowNotFoundException() {

        //When Then
        assertThrows(NotFoundException.class, () -> clientService.getClient("invalidEmail"));

    }

    @Test
    @Transactional
    void addFollowingCaretaker_ShouldAddCaretakersSuccessfully() {
        // Given
        Client client = PersistenceUtils.addClient(appUserRepository, clientRepository);
        List<Caretaker> caretakers = addTwoCaretakers();

        client.getFollowingCaretakers().add(caretakers.get(0));
        clientRepository.saveAndFlush(client);

        // When
        Set<String> result = clientService.addFollowingCaretaker(client.getEmail(), caretakers.get(1).getEmail());

        // Then
        assertNotNull(result);
        assertTrue(result.contains(caretakers.get(0).getEmail()));
        assertTrue(result.contains(caretakers.get(1).getEmail()));
    }

    @Test
    @Transactional
    void addFollowingCaretaker_WhenClientTriesToFollowItself_ShouldThrowIllegalActionException() {
        // Given
        Client client = PersistenceUtils.addClient(appUserRepository, clientRepository);
        String caretakerEmails = client.getEmail();

        // When Then
        assertThrows(IllegalActionException.class,
                () -> clientService.addFollowingCaretaker(client.getEmail(), caretakerEmails));

    }

    @Test
    @Transactional
    void addFollowingCaretaker_WhenCaretakersAreAlreadyFollowed_ShouldThrowIllegalActionException() {
        // Given
        Client client = PersistenceUtils.addClient(appUserRepository, clientRepository);
        Caretaker caretaker = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository,
                createMockCaretaker("caretaker1")
        );

        client.getFollowingCaretakers().add(caretaker);
        clientRepository.saveAndFlush(client);

        // When Then
        assertThrows(IllegalActionException.class,
                () -> clientService.addFollowingCaretaker(client.getEmail(), caretaker.getEmail()));
    }

    @Test
    @Transactional
    void addFollowingCaretaker_WhenCaretakerDoesNotExist_ShouldThrowNotFoundException() {
        // Given
        Client client = PersistenceUtils.addClient(appUserRepository, clientRepository);
        String caretakerEmail = "nonexistent@example.com";

        // When Then
        assertThrows(NotFoundException.class,
                () -> clientService.addFollowingCaretaker(client.getEmail(), caretakerEmail));
    }

    @Test
    @Transactional
    void removeFollowingCaretaker_ShouldRemoveCaretakersSuccessfully() {
        // Given
        Client client = PersistenceUtils.addClient(appUserRepository, clientRepository);
        List<Caretaker> caretakers = addTwoCaretakers();

        client.getFollowingCaretakers().add(caretakers.get(0));
        client.getFollowingCaretakers().add(caretakers.get(1));
        clientRepository.saveAndFlush(client);

        // When
        Set<String> result = clientService.removeFollowingCaretaker(client.getEmail(), caretakers.get(0).getEmail());

        // Then
        assertNotNull(result);
        assertFalse(result.contains(caretakers.get(0).getEmail()));
        assertTrue(result.contains(caretakers.get(1).getEmail()));
    }

    @Test
    @Transactional
    void removeFollowingCaretaker_WhenClientTriesToUnfollowNotFollowedCaretaker_ShouldThrowIllegalActionException() {
        // Given
        Client client = PersistenceUtils.addClient(appUserRepository, clientRepository);
        List<Caretaker> caretakers = addTwoCaretakers();

        client.getFollowingCaretakers().add(caretakers.get(0));
        clientRepository.saveAndFlush(client);

        // When Then
        assertThrows(IllegalActionException.class,
                () -> clientService.removeFollowingCaretaker(client.getEmail(), caretakers.get(1).getEmail()));

    }

    @Test
    @Transactional
    void getFollowedCaretakers_shouldReturnProperAnswer() {

        //Given
        Client client = PersistenceUtils.addClient(appUserRepository, clientRepository);
        List<Caretaker> caretakers = addTwoCaretakers();
        PersistenceUtils.addFollowingCaretakersToClient(clientRepository, client, new HashSet<>(caretakers));

        //When
        Set<CaretakerDTO> result = clientService.getFollowedCaretakers(client.getEmail());

        //Then
        assertNotNull(result);
        assertEquals(2, result.size());

    }

    private List<Caretaker> addTwoCaretakers() {
        Caretaker caretaker1 = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository,
                createMockCaretaker("caretaker1")
        );
        Caretaker caretaker2 = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository,
                createMockCaretaker("caretaker2")
        );
        return List.of(caretaker1, caretaker2);
    }

}
