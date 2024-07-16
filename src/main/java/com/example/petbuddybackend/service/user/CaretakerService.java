package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.CaretakerSearchCriteria;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.CaretakerRepository;
import com.example.petbuddybackend.service.mapper.CaretakerMapper;
import com.example.petbuddybackend.utils.specification.CaretakerSpecificationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CaretakerService {

    private final CaretakerRepository caretakerRepository;
    private final CaretakerMapper caretakerMapper = CaretakerMapper.INSTANCE;

    public Page<CaretakerDTO> getCaretakers(Pageable pageable, CaretakerSearchCriteria filters) {
        Specification<Caretaker> spec = CaretakerSpecificationUtils.toSpecification(filters);

        return caretakerRepository
                .findAll(spec, pageable)
                .map(caretakerMapper::mapToCaretakerDTO);
    }
}
