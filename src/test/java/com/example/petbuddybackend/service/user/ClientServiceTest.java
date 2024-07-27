package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.AppUserRepository;
import com.example.petbuddybackend.repository.ClientRepository;
import com.example.petbuddybackend.testutils.MockUtils;
import com.example.petbuddybackend.utils.exception.throweable.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void getClientIdByUsername_shouldSucceed() {
        Client client = MockUtils.createMockClient();

        AppUser accountData = appUserRepository.saveAndFlush(client.getAccountData());
        client.setId(accountData.getId());
        clientRepository.saveAndFlush(client);

        Long clientId = clientService.getClientIdByUsername(client.getAccountData().getUsername());
        assertNotNull(clientId);
    }

    @Test
    void getClientIdByUsername_noSuchClientPresent_shouldThrowNotFoundException() {
        assertThrows(
                NotFoundException.class,
                () -> clientService.getClientIdByUsername("username")
        );
    }
}
