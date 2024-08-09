package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.offer.CaretakerOfferDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.CaretakerSearchCriteria;
import com.example.petbuddybackend.entity.offer.CaretakerOffer;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.AnimalPreferenceRepository;
import com.example.petbuddybackend.repository.CaretakerOfferRepository;
import com.example.petbuddybackend.repository.CaretakerRepository;
import com.example.petbuddybackend.service.mapper.CaretakerMapper;
import com.example.petbuddybackend.service.mapper.CaretakerOfferMapper;
import com.example.petbuddybackend.utils.exception.user.CaretakerNotFoundException;
import com.example.petbuddybackend.utils.specification.CaretakerSpecificationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CaretakerService {

    private final CaretakerRepository caretakerRepository;
    private final CaretakerOfferRepository caretakerOfferRepository;
    private final AnimalPreferenceRepository animalPreferenceRepository;
    private final CaretakerMapper caretakerMapper = CaretakerMapper.INSTANCE;
    private final CaretakerOfferMapper caretakerOfferMapper = CaretakerOfferMapper.INSTANCE;

    @Transactional
    public Page<CaretakerDTO> getCaretakers(Pageable pageable, CaretakerSearchCriteria filters) {
        Specification<Caretaker> spec = CaretakerSpecificationUtils.toSpecification(filters);

        return caretakerRepository
                .findAll(spec, pageable)
                .map(caretakerMapper::mapToCaretakerDTO);
    }

    public List<CaretakerOfferDTO> addOrEditCaretakerOffersForCaretaker(List<CaretakerOfferDTO> caretakerOffers, String name) {
        return caretakerOffers.stream()
                .map(caretakerOffer -> addOrEditCaretakerOfferForCaretaker(caretakerOffer, name))
                .toList();
    }

    public CaretakerOfferDTO addOrEditCaretakerOfferForCaretaker(CaretakerOfferDTO caretakerOffer, String caretakerEmail) {

        assertCaretakerExists(caretakerEmail);

        CaretakerOffer createdCaretakerOffer = getExistingOfferOrCreate(caretakerEmail, caretakerOffer.animalPreference().id());
        createdCaretakerOffer.setDailyPrice(caretakerOffer.dailyPrice());
        return caretakerOfferMapper.mapToCaretakerOfferDTO(caretakerOfferRepository.save(createdCaretakerOffer));

    }

    private void assertCaretakerExists(String caretakerEmail) {
        if(!caretakerRepository.existsById(caretakerEmail)) {
            throw new CaretakerNotFoundException(caretakerEmail);
        }
    }

    private CaretakerOffer getExistingOfferOrCreate(String caretakerEmail, Long animalPreferenceId) {
        return caretakerOfferRepository
                .findByCaretaker_EmailAndAnimalPreference_Id(caretakerEmail, animalPreferenceId)
                .orElse(
                        CaretakerOffer.builder()
                                .caretaker(caretakerRepository.getReferenceById(caretakerEmail))
                                .animalPreference(animalPreferenceRepository.getReferenceById(animalPreferenceId))
                                .build()
                );
    }

}
