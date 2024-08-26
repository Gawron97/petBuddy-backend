package com.example.petbuddybackend.service.care;

import com.example.petbuddybackend.dto.care.CareDTO;
import com.example.petbuddybackend.dto.care.CreateCareDTO;
import com.example.petbuddybackend.dto.care.UpdateCareDTO;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.repository.CareRepository;
import com.example.petbuddybackend.service.animal.AnimalService;
import com.example.petbuddybackend.service.mapper.CareMapper;
import com.example.petbuddybackend.service.user.CaretakerService;
import com.example.petbuddybackend.service.user.ClientService;
import com.example.petbuddybackend.utils.exception.throweable.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.keycloak.common.util.CollectionUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CareService {

    private final CareRepository careRepository;
    private final AnimalService animalService;
    private final CaretakerService caretakerService;
    private final ClientService clientService;
    private final CareMapper careMapper;

    public CareDTO makeReservation(CreateCareDTO createCare, String clientEmail) {

        assertCurrentlyLoggedInClientMakingReservation(createCare.clientEmail(), clientEmail);
        assertClientIsNotCaretaker(createCare.clientEmail(), createCare.caretakerEmail());
        assertEndCareDateIsAfterStartCareDate(createCare.careStart(), createCare.careEnd());
        Set<AnimalAttribute> animalAttributes = new HashSet<>();
        if(CollectionUtil.isNotEmpty(createCare.animalAttributeIds())) {
            animalAttributes = animalService.getAnimalAttributes(createCare.animalAttributeIds());
            assertAnimalAttributesMatchAnimalType(animalAttributes, createCare.animalType());
        }

        Care care = createCare(createCare, animalAttributes);
        return careMapper.mapToCareDTO(careRepository.save(care));

    }

    private Care createCare(CreateCareDTO createCare, Set<AnimalAttribute> animalAttributes) {

        return Care.builder()
                .caretakerStatus(CareStatus.PENDING)
                .clientStatus(CareStatus.PENDING)
                .careStart(createCare.careStart())
                .careEnd(createCare.careEnd())
                .description(createCare.description())
                .dailyPrice(createCare.dailyPrice())
                .animal(animalService.getAnimal(createCare.animalType()))
                .animalAttributes(animalAttributes)
                .caretaker(caretakerService.getCaretaker(createCare.caretakerEmail()))
                .client(clientService.getClient(createCare.clientEmail()))
                .build();

    }

    private void assertCurrentlyLoggedInClientMakingReservation(String clientEmailToMakeReservation,
                                                                String loggedInClientEmail) {

        if(!clientEmailToMakeReservation.equals(loggedInClientEmail)) {
            throw new IllegalActionException("Client can only make reservation for themselves");
        }

    }

    private void assertClientIsNotCaretaker(String clientEmail, String caretakerEmail) {

        if(clientEmail.equals(caretakerEmail)) {
            throw new IllegalActionException("Client cannot make reservation for care from his offer");
        }

    }

    private void assertEndCareDateIsAfterStartCareDate(LocalDate careStart, LocalDate careEnd) {
        if(careEnd.isBefore(careStart)) {
            throw new IllegalActionException("End care date must be after start care date");
        }
    }

    private void assertAnimalAttributesMatchAnimalType(Set<AnimalAttribute> animalAttributes, String animalType) {
        if(animalAttributes.stream()
                .anyMatch(animalAttribute -> !animalAttribute.getAnimal().getAnimalType().equals(animalType))) {
            throw new IllegalActionException("Animal attributes must match animal type");
        }
    }

    public CareDTO updateCare(Long careId, UpdateCareDTO updateCare, String userEmail) {

        Care care = getCare(careId);
        assertCaretakerEditCare(care.getCaretaker().getEmail(), userEmail);
        assertEndCareDateIsAfterStartCareDate(updateCare.careStart(), updateCare.careEnd());
        careMapper.updateCareFromDTO(updateCare, care);
        return careMapper.mapToCareDTO(careRepository.save(care));

    }

    private void assertCaretakerEditCare(String caretakerEmail, String userEmail) {
        if(!caretakerEmail.equals(userEmail)) {
            throw new IllegalActionException("Only caretaker can edit care");
        }
    }

    private Care getCare(Long careId) {
        return careRepository.findById(careId)
                .orElseThrow(() -> new NotFoundException("Care not found"));
    }

}
