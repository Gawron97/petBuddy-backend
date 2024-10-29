package com.example.petbuddybackend.scheduled;

import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.repository.care.CareRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testconfig.TestDataConfiguration;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import com.example.petbuddybackend.testutils.mock.MockCareProvider;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = TestDataConfiguration.class)
public class CareScheduledIntegrationTest {

    @Autowired
    private CareScheduled careScheduled;

    @Autowired
    private CareRepository careRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CaretakerRepository caretakerRepository;

    @Autowired
    private AnimalRepository animalRepository;

    private Set<Pair<CareStatus, CareStatus>> testStatuses;
    private Client client;
    private Caretaker caretaker;


    @BeforeEach
    void setUp() {
        client = PersistenceUtils.addClient(
                appUserRepository,
                clientRepository,
                MockUserProvider.createMockClient()
        );

        caretaker = PersistenceUtils.addCaretaker(
                caretakerRepository,
                appUserRepository,
                MockUserProvider.createMockCaretaker()
        );

        testStatuses = Set.of(
                // CareStatuses to be obsoleted
                Pair.of(CareStatus.PENDING, CareStatus.PENDING),
                Pair.of(CareStatus.PENDING, CareStatus.ACCEPTED),
                Pair.of(CareStatus.ACCEPTED, CareStatus.PENDING),
                Pair.of(CareStatus.AWAITING_PAYMENT, CareStatus.AWAITING_PAYMENT),

                // CareStatuses to be kept
                Pair.of(CareStatus.PAID, CareStatus.PAID),
                Pair.of(CareStatus.CANCELLED, CareStatus.CANCELLED)
        );
    }

    @AfterEach
    void tearDown() {
        careRepository.deleteAll();
        clientRepository.deleteAll();
        caretakerRepository.deleteAll();
    }


    @Test
    void terminateCares_shouldTerminateProperCares() {
        // Given
        setupOutdatedCares();

        // Then
        careScheduled.terminateCares();

        List<Care> cares = careRepository.findAll();

        List<Care> outdatedCares = cares.stream()
                .filter(care -> care.getClientStatus() == CareStatus.OUTDATED)
                .toList();

        List<Care> paidCares = cares.stream()
                .filter(care -> care.getClientStatus() == CareStatus.PAID)
                .toList();

        List<Care> cancelledCares = cares.stream()
                .filter(care -> care.getClientStatus() == CareStatus.CANCELLED)
                .toList();

        assertEquals(4, outdatedCares.size());
        assertEquals(1, paidCares.size());
        assertEquals(1, cancelledCares.size());
    }

    @Test
    void terminateCares_shouldNotTerminateAnyNonOutdatedCare() {
        // Given
        setupNonOutdatedCares();

        // Then
        careScheduled.terminateCares();

        List<Care> cares = careRepository.findAll();

        List<Care> outdatedCares = cares.stream()
                .filter(care -> care.getClientStatus() == CareStatus.OUTDATED)
                .toList();

        List<Care> paidCares = cares.stream()
                .filter(care -> care.getClientStatus() == CareStatus.PAID)
                .toList();

        List<Care> cancelledCares = cares.stream()
                .filter(care -> care.getClientStatus() == CareStatus.CANCELLED)
                .toList();

        assertEquals(0, outdatedCares.size());
        assertEquals(1, paidCares.size());
        assertEquals(1, cancelledCares.size());
    }

    private void setupOutdatedCares() {
        Animal animal = animalRepository.findById("DOG").get();

        testStatuses.stream()
                .map(pair -> MockCareProvider.createMockCare(
                        caretaker, client, animal, pair.getFirst(), pair.getSecond())
                )
                .map(care -> {
                    care.setCareStart(LocalDate.now().minusDays(1));
                    return care;
                })
                .map(care -> PersistenceUtils.addCare(careRepository, care))
                .toList();
    }

    private void setupNonOutdatedCares() {
        Animal animal = animalRepository.findById("DOG").get();

        testStatuses.stream()
                .map(pair -> MockCareProvider.createMockCare(
                        caretaker, client, animal, pair.getFirst(), pair.getSecond()))
                .map(care -> {
                    care.setCareStart(LocalDate.now().plusDays(1));
                    return care;
                })
                .map(care -> PersistenceUtils.addCare(careRepository, care))
                .toList();
    }
}
