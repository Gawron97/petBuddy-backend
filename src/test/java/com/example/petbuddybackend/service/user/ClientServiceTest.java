package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testutils.MockUtils;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import static com.example.petbuddybackend.testutils.MockUtils.createJwtToken;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ClientServiceTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    private UserService userService;


    @AfterEach
    void tearDown() {
        appUserRepository.deleteAll();
    }


    @Test
    void checkClientExists_shouldReturnTrue() {
        Client client = MockUtils.createMockClient();

        AppUser accountData = PersistenceUtils.addAppUser(appUserRepository, client.getAccountData());
        client.setEmail(accountData.getEmail());
        clientRepository.saveAndFlush(client);

        boolean clientExists = clientService.clientExists(client.getAccountData().getEmail());
        assertNotNull(clientExists);
    }

    @Test
    void checkClientExists_noSuchClientPresent_shouldReturnFalse() {
        boolean clientExists = clientService.clientExists("invalidEmail");
        assertNotNull(clientExists);
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

}
