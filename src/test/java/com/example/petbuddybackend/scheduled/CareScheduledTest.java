package com.example.petbuddybackend.scheduled;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.repository.care.CareRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class CareScheduledTest {

    @Autowired
    private CareScheduled careScheduled;

    @MockBean
    private CareRepository careRepository;

    @Test
    void terminateCares_shouldTerminateProperCares() {

        // Given
        List<Care> obsoleteMockCares = new ArrayList<>();
        List<CareStatus> statusesToObsolete = List.of(CareStatus.PENDING, CareStatus.ACCEPTED, CareStatus.AWAITING_PAYMENT);

        for(CareStatus clientStatus: statusesToObsolete) {
            for(CareStatus caretakerStatus: statusesToObsolete) {
                Care care = Care.builder()
                        .clientStatus(clientStatus)
                        .caretakerStatus(caretakerStatus)
                        .build();

                obsoleteMockCares.add(care);
            }
        }

        // When
        when(careRepository.findAllByCaretakerStatusNotInOrClientStatusNotIn(any(), any()))
                .thenReturn(obsoleteMockCares);

        // Then
        careScheduled.terminateCares();
        verify(careRepository, times(1)).saveAll(eq(obsoleteMockCares));

        obsoleteMockCares.forEach(care -> {
            assertEquals(CareStatus.OUTDATED, care.getClientStatus());
            assertEquals(CareStatus.OUTDATED, care.getCaretakerStatus());
        });
    }
}
