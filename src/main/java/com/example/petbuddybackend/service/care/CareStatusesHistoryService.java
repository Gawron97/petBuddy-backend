package com.example.petbuddybackend.service.care;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatusesHistory;
import com.example.petbuddybackend.repository.care.CareStatusesHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class CareStatusesHistoryService {

    private final CareStatusesHistoryRepository careStatusesHistoryRepository;

    public void addCareStatusesHistory(Care care) {
        careStatusesHistoryRepository.save(careStatusesHistory(care));
    }

    private CareStatusesHistory careStatusesHistory(Care care) {
        return CareStatusesHistory
                .builder()
                .createdAt(ZonedDateTime.now())
                .clientStatus(care.getClientStatus())
                .caretakerStatus(care.getCaretakerStatus())
                .care(care)
                .build();
    }
}
