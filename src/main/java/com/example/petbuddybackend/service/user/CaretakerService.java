package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.repository.CaretakerRepository;
import com.example.petbuddybackend.service.mapper.CaretakerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CaretakerService {

    private final CaretakerRepository caretakerRepository;
    private final CaretakerMapper caretakerMapper = CaretakerMapper.INSTANCE;

    public Page<CaretakerDTO> getCaretakers(Pageable pageable) {
        return caretakerRepository
                .findAll(pageable)
                .map(caretaker -> caretakerMapper.mapToCaretakerDTO(caretaker));
    }
}
