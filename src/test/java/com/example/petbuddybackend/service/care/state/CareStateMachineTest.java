package com.example.petbuddybackend.service.care.state;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.utils.exception.throweable.general.StateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class CareStateMachineTest {

    private static final Duration CONFIRM_WINDOW;
    private static final int CONFIRM_WINDOW_DAYS;

    @Autowired
    private CareStateMachine careStateMachine;

    static {
        CONFIRM_WINDOW_DAYS = 60;
        CONFIRM_WINDOW = Duration.ofDays(CONFIRM_WINDOW_DAYS);
    }

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(careStateMachine, "CONFIRM_CARE_TIME_WINDOW", CONFIRM_WINDOW);
    }

    @Test
    void transitionToEditCare_shouldSucceed() {
        Care care = careOfStatuses(CareStatus.PENDING, CareStatus.PENDING);
        careStateMachine.transitionToEditCare(care);
    }

    @Test
    void transitionToEditCare_caretakerIsNotPending_shouldThrowException() {
        Care care = careOfStatuses(CareStatus.PENDING, CareStatus.ACCEPTED);
        assertThrows(StateTransitionException.class, () -> careStateMachine.transitionToEditCare(care));
    }

    @Test
    void transitionToEditCare_clientIsNotPendingOrAccepted_shouldThrowException() {
        Care care1 = careOfStatuses(CareStatus.READY_TO_PROCEED, CareStatus.PENDING);
        assertThrows(StateTransitionException.class, () -> careStateMachine.transitionToEditCare(care1));

        Care care2 = careOfStatuses(CareStatus.CONFIRMED, CareStatus.PENDING);
        assertThrows(StateTransitionException.class, () -> careStateMachine.transitionToEditCare(care2));
    }

    @Test
    void transition_caretakerTransitionsToAccepted_shouldSucceed() {
        Care care = careOfStatuses(CareStatus.ACCEPTED, CareStatus.PENDING);
        Care updatedCare = careStateMachine.transition(Role.CARETAKER, care, CareStatus.ACCEPTED);
        assertEquals(CareStatus.READY_TO_PROCEED, updatedCare.getCaretakerStatus());
    }

    @Test
    void transition_caretakerTransitionsToAccepted_clientNotAccepted_shouldThrow() {
        Care care = careOfStatuses(CareStatus.PENDING, CareStatus.PENDING);
        assertThrows(StateTransitionException.class, () -> careStateMachine.transition(Role.CARETAKER, care, CareStatus.ACCEPTED));
    }

    @ParameterizedTest
    @MethodSource("provideValidCareStartDates")
    void transition_caretakerMarksCareAsConfirmed_shouldSucceed(LocalDate startCareDate) {
        Care care = careOfStatuses(CareStatus.READY_TO_PROCEED, CareStatus.READY_TO_PROCEED, startCareDate);
        careStateMachine.transition(Role.CARETAKER, care, CareStatus.CONFIRMED);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCareStartDates")
    void transition_caretakerMarksCareAsConfirmed_invalidStartDate_shouldThrow(LocalDate startCareDate) {
        Care care = careOfStatuses(CareStatus.READY_TO_PROCEED, CareStatus.READY_TO_PROCEED, startCareDate);

        assertThrows(StateTransitionException.class,
                () -> careStateMachine.transition(Role.CARETAKER, care, CareStatus.CONFIRMED));
    }

    private Care careOfStatuses(CareStatus clientStatus, CareStatus caretakerStatus, LocalDate careStart) {
        return Care.builder()
                .clientStatus(clientStatus)
                .caretakerStatus(caretakerStatus)
                .careStart(careStart)
                .build();
    }

    private Care careOfStatuses(CareStatus clientStatus, CareStatus caretakerStatus) {
        return careOfStatuses(clientStatus, caretakerStatus, LocalDate.now());
    }

    private static Stream<LocalDate> provideValidCareStartDates() {
        return Stream.of(
                LocalDate.now().minusDays(CONFIRM_WINDOW_DAYS),
                LocalDate.now().minusDays(CONFIRM_WINDOW_DAYS -1),
                LocalDate.now()
        );
    }

    private static Stream<LocalDate> provideInvalidCareStartDates() {
        return Stream.of(
                LocalDate.now().minusDays(CONFIRM_WINDOW_DAYS +1),
                LocalDate.now().plusDays(1)
        );
    }
}
