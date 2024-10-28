package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.criteriaSearch.CaretakerSearchCriteria;
import com.example.petbuddybackend.dto.offer.OfferFilterDTO;
import com.example.petbuddybackend.dto.user.CaretakerComplexInfoDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.ModifyCaretakerDTO;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.service.mapper.CaretakerMapper;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.specification.CaretakerSpecificationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CaretakerService {

    private final static String CARETAKER = "Caretaker";

    private final CaretakerRepository caretakerRepository;
    private final CaretakerMapper caretakerMapper = CaretakerMapper.INSTANCE;

    private final UserService userService;

    @Transactional(readOnly = true)
    public Page<CaretakerDTO> getCaretakers(Pageable pageable,
                                            CaretakerSearchCriteria filters,
                                            Set<OfferFilterDTO> offerFilters) {
        Specification<Caretaker> spec = CaretakerSpecificationUtils.toSpecification(filters, offerFilters);

        return caretakerRepository.findAll(spec, pageable)
                .map(this::renewCaretakerProfilePicture)
                .map(caretakerMapper::mapToCaretakerDTO);
    }

    public CaretakerComplexInfoDTO getCaretaker(String caretakerEmail) {
        Caretaker caretaker = getCaretakerByEmail(caretakerEmail);
        renewCaretakerProfilePicture(caretaker);
        return caretakerMapper.mapToCaretakerComplexInfoDTO(caretaker);
    }

    public Caretaker getCaretakerByEmail(String caretakerEmail) {
        return caretakerRepository.findById(caretakerEmail)
                .orElseThrow(() -> NotFoundException.withFormattedMessage(CARETAKER, caretakerEmail));
    }

    public boolean caretakerExists(String caretakerEmail) {
        return caretakerRepository.existsById(caretakerEmail);
    }

    public CaretakerComplexInfoDTO addCaretaker(ModifyCaretakerDTO caretakerDTO, String email) {
        assertCaretakerNotExists(email);
        AppUser appUser = userService.getAppUser(email);
        Caretaker caretaker = caretakerMapper.mapToCaretaker(caretakerDTO, appUser);

        renewCaretakerProfilePicture(caretaker);
        return caretakerMapper.mapToCaretakerComplexInfoDTO(caretakerRepository.save(caretaker));
    }

    public CaretakerComplexInfoDTO editCaretaker(ModifyCaretakerDTO caretakerDTO, String email) {
        AppUser appUser = userService.getAppUser(email);
        Caretaker caretaker = getCaretakerByEmail(appUser.getEmail());

        renewCaretakerProfilePicture(caretaker);
        caretakerMapper.updateCaretakerFromDTO(caretakerDTO, caretaker);
        return caretakerMapper.mapToCaretakerComplexInfoDTO(caretakerRepository.save(caretaker));
    }

    private Caretaker renewCaretakerProfilePicture(Caretaker caretaker) {
        userService.renewProfilePicture(caretaker.getAccountData());
        return caretaker;
    }

    private void assertCaretakerNotExists(String caretakerEmail) {
        if (caretakerExists(caretakerEmail)) {
            throw new IllegalActionException("Caretaker with email " + caretakerEmail + " already exists");
        }
    }
}
