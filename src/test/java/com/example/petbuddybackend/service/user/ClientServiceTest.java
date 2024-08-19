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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ClientServiceTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientService clientService;


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
}
