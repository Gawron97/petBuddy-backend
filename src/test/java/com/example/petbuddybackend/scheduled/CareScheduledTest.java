package com.example.petbuddybackend.scheduled;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.repository.care.CareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;

import static com.example.petbuddybackend.testutils.mock.MockAnimalProvider.createMockAnimal;
import static com.example.petbuddybackend.testutils.mock.MockCareProvider.createMockCare;
import static com.example.petbuddybackend.testutils.mock.MockUserProvider.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CareScheduledTest {

    @InjectMocks
    private CareScheduled careScheduled;

    @Mock
    private CareRepository careRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void terminateCares_shouldTerminateProperCares() {

        // Given
        List<Care> cares = getMockCares();
        cares.get(0).setClientStatus(CareStatus.CANCELLED);
        cares.get(1).setClientStatus(CareStatus.OUTDATED);
        cares.get(1).setCaretakerStatus(CareStatus.OUTDATED);
        cares.get(2).setClientStatus(CareStatus.PAID);
        cares.get(4).setCareStart(LocalDate.now());

        // When
        when(careRepository.findAllByCaretakerStatusNotInOrClientStatusNotIn(any(), any())).thenReturn(List.of(
                cares.get(3),
                cares.get(4)
        ));

        // Then
        careScheduled.terminateCares();

        verify(careRepository, times(2)).save(any(Care.class));

    }

    private List<Care> getMockCares() {
        return List.of(
                createMockCare(
                        createMockCaretaker(
                                "name", "surname", "email1", createMockAddress()
                        ),
                        createMockClient(
                                "name", "surname", "email2"
                        ),
                        createMockAnimal("DOG")
                ),
                createMockCare(
                        createMockCaretaker(
                                "name", "surname", "email3", createMockAddress()
                        ),
                        createMockClient(
                                "name", "surname", "email4"
                        ),
                        createMockAnimal("DOG")
                ),
                createMockCare(
                        createMockCaretaker(
                                "name", "surname", "email5", createMockAddress()
                        ),
                        createMockClient(
                                "name", "surname", "email6"
                        ),
                        createMockAnimal("DOG")
                ),
                createMockCare(
                        createMockCaretaker(
                                "name", "surname", "email7", createMockAddress()
                        ),
                        createMockClient(
                                "name", "surname", "email8"
                        ),
                        createMockAnimal("DOG")
                ),
                createMockCare(
                        createMockCaretaker(
                                "name", "surname", "email9", createMockAddress()
                        ),
                        createMockClient(
                                "name", "surname", "email10"
                        ),
                        createMockAnimal("DOG")
                )
        );
    }

}
