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
import com.example.petbuddybackend.service.care.state.CareStateMachine;
import com.example.petbuddybackend.testconfig.TestDataConfiguration;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import com.example.petbuddybackend.testutils.mock.MockCareProvider;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = TestDataConfiguration.class)
public class CareScheduledIntegrationTest {

    private static final Duration CONFIRM_WINDOW;
    private static final int CONFIRM_WINDOW_DAYS;

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
    private CareStateMachine careStateMachine;

    @Autowired
    private AnimalRepository animalRepository;

    private Set<Pair<CareStatus, CareStatus>> testStatuses;
    private Client client;
    private Caretaker caretaker;

    static {
        CONFIRM_WINDOW_DAYS = 60;
        CONFIRM_WINDOW = Duration.ofDays(CONFIRM_WINDOW_DAYS);
    }


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
                Pair.of(CareStatus.READY_TO_PROCEED, CareStatus.READY_TO_PROCEED),

                // CareStatuses to be kept
                Pair.of(CareStatus.CONFIRMED, CareStatus.CONFIRMED),
                Pair.of(CareStatus.CANCELLED, CareStatus.CANCELLED)
        );

        ReflectionTestUtils.setField(careStateMachine, "CONFIRM_CARE_TIME_WINDOW", CONFIRM_WINDOW);
    }

    @AfterEach
    void tearDown() {
        careRepository.deleteAll();
        clientRepository.deleteAll();
        caretakerRepository.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("careStartTimeWithinWindow")
    void terminateCares_dateWithinConfirmTimeWindow_shouldNotObsoleteCare(LocalDate startCare) {
        setupCaresWithCareStart(startCare);

        careScheduled.terminateCares();

        List<Care> cares = careRepository.findAll();

        List<Care> outdatedCares = cares.stream()
                .filter(care -> care.getClientStatus() == CareStatus.OUTDATED)
                .toList();

        List<Care> confirmedCares = cares.stream()
                .filter(care -> care.getClientStatus() == CareStatus.CONFIRMED)
                .toList();

        List<Care> cancelledCares = cares.stream()
                .filter(care -> care.getClientStatus() == CareStatus.CANCELLED)
                .toList();

        assertEquals(6, cares.size());
        assertEquals(0, outdatedCares.size());
        assertEquals(1, confirmedCares.size());
        assertEquals(1, cancelledCares.size());
    }

    @ParameterizedTest
    @MethodSource("careStartTimeAfterWindow")
    void terminateCares_dateAfterConfirmTimeWindow_shouldNotObsoleteCare(LocalDate startCare) {
        setupCaresWithCareStart(startCare);

        careScheduled.terminateCares();

        List<Care> cares = careRepository.findAll();

        List<Care> outdatedCares = cares.stream()
                .filter(care -> care.getClientStatus() == CareStatus.OUTDATED)
                .toList();

        List<Care> confirmedCares = cares.stream()
                .filter(care -> care.getClientStatus() == CareStatus.CONFIRMED)
                .toList();

        List<Care> cancelledCares = cares.stream()
                .filter(care -> care.getClientStatus() == CareStatus.CANCELLED)
                .toList();

        assertEquals(6, cares.size());
        assertEquals(4, outdatedCares.size());
        assertEquals(1, confirmedCares.size());
        assertEquals(1, cancelledCares.size());
    }

    private void setupCaresWithCareStart(LocalDate careStart) {
        Animal animal = animalRepository.findById("DOG").get();

        testStatuses.stream()
                .map(pair -> MockCareProvider.createMockCare(
                        caretaker, client, animal, pair.getFirst(), pair.getSecond())
                )
                .map(care -> {
                    care.setCareStart(careStart);
                    return care;
                })
                .map(care -> PersistenceUtils.addCare(careRepository, care))
                .toList();
    }

    static Stream<Arguments> careStartTimeWithinWindow() {
        return Stream.of(
                Arguments.of(LocalDate.now().minusDays(CONFIRM_WINDOW_DAYS)),
                Arguments.of(LocalDate.now().minusDays(CONFIRM_WINDOW_DAYS == 0 ? 1 : CONFIRM_WINDOW_DAYS / 2)),
                Arguments.of(LocalDate.now().plusDays(1)),
                Arguments.of(LocalDate.now())
        );
    }

    static Stream<Arguments> careStartTimeAfterWindow() {
        return Stream.of(
                Arguments.of(LocalDate.now().minusDays(CONFIRM_WINDOW_DAYS + 1)),
                Arguments.of(LocalDate.now().minusDays(CONFIRM_WINDOW_DAYS + 100))
        );
    }
}
