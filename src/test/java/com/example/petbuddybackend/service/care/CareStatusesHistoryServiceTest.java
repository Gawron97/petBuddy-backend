package com.example.petbuddybackend.service.care;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.repository.care.CareRepository;
import com.example.petbuddybackend.repository.care.CareStatusesHistoryRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testconfig.TestDataConfiguration;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = TestDataConfiguration.class)
public class CareStatusesHistoryServiceTest {

    @Autowired
    private CareStatusesHistoryService careStatusesHistoryService;

    @Autowired
    private CareStatusesHistoryRepository careStatusesHistoryRepository;

    @Autowired
    private CareRepository careRepository;

    @Autowired
    private CaretakerRepository caretakerRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        careStatusesHistoryRepository.deleteAll();
        careRepository.deleteAll();
        caretakerRepository.deleteAll();
        clientRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void testAddCareStatusesHistory() {

        //Given
        Caretaker caretaker = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository);
        Client client = PersistenceUtils.addClient(appUserRepository, clientRepository);
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());

        //When
        careStatusesHistoryService.addCareStatusesHistory(care);

        //Then
        assertEquals(1, careStatusesHistoryRepository.findAll().size());

        transactionTemplate.execute(status -> {
            Care editedCare = careRepository.findById(care.getId()).get();
            assertEquals(1, editedCare.getCareStatusesHistory().size());
            return null;
        });

    }

}
