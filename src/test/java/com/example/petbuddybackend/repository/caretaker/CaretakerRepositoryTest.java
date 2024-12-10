package com.example.petbuddybackend.repository.caretaker;

import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class CaretakerRepositoryTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private CaretakerRepository caretakerRepository;

    @Autowired
    private ClientRepository clientRepository;

    private Client client;
    private Caretaker caretaker;

    @BeforeEach
    void setUp() {
        client = PersistenceUtils.addClient(appUserRepository, clientRepository);
        caretaker = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository);
        client.getFollowingCaretakers().add(caretaker);
        clientRepository.save(client);
    }

    @AfterEach
    void tearDown() {
        appUserRepository.deleteAll();
    }

    @Test
    void testisCaretakerFollowedByClient_ShouldReturnProperAnswer() {

        boolean result = caretakerRepository.isCaretakerFollowedByClient(
                caretaker.getEmail(),
                client.getEmail()
        );
        assertTrue(result);

    }


}
