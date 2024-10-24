package com.example.petbuddybackend.service.care;

import com.example.petbuddybackend.dto.care.CareDTO;
import com.example.petbuddybackend.dto.care.CreateCareDTO;
import com.example.petbuddybackend.dto.care.UpdateCareDTO;
import com.example.petbuddybackend.dto.criteriaSearch.CareSearchCriteria;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.notification.ObjectType;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.repository.care.CareRepository;
import com.example.petbuddybackend.service.animal.AnimalService;
import com.example.petbuddybackend.service.mapper.CareMapper;
import com.example.petbuddybackend.service.notification.NotificationService;
import com.example.petbuddybackend.service.user.CaretakerService;
import com.example.petbuddybackend.service.user.ClientService;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.specification.CareSpecificationUtils;
import lombok.RequiredArgsConstructor;
import org.keycloak.common.util.CollectionUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CareService {

    @Value("${notification.care.reservation}")
    private String reservationMessage;

    @Value("${notification.care.update_reservation}")
    private String updateReservationMessage;

    @Value("${notification.care.accepted_reservation}")
    private String acceptReservationMessage;

    @Value("${notification.care.rejected_reservation}")
    private String rejectReservationMessage;
    
    private final CareRepository careRepository;
    private final AnimalService animalService;
    private final CaretakerService caretakerService;
    private final ClientService clientService;
    private final NotificationService notificationService;
    private final CareMapper careMapper = CareMapper.INSTANCE;

    public CareDTO makeReservation(CreateCareDTO createCare, String clientEmail, ZoneId timeZone) {

        assertLoggedInUserIsClient(createCare.clientEmail(), clientEmail,
                "Client can only make reservation for themselves");
        assertClientIsNotCaretaker(createCare.clientEmail(), createCare.caretakerEmail());
        assertEndCareDateIsAfterStartCareDate(createCare.careStart(), createCare.careEnd());
        Set<AnimalAttribute> animalAttributes = new HashSet<>();
        if(CollectionUtil.isNotEmpty(createCare.animalAttributeIds())) {
            animalAttributes = animalService.getAnimalAttributesOfAnimal(createCare.animalAttributeIds());
            assertAnimalAttributesMatchAnimalType(animalAttributes, createCare.animalType());
        }

        Care care = createCare(createCare, animalAttributes);
        Care savedCare = careRepository.save(care);
        notificationService.addNotificationForCaretakerAndSend(
                savedCare.getId(),
                ObjectType.CARE,
                savedCare.getCaretaker(),
                "message.care.reservation",
                Set.of(care.getClient().getEmail())
        );
        return careMapper.mapToCareDTO(savedCare, timeZone);

    }

    public CareDTO updateCare(Long careId, UpdateCareDTO updateCare, String userEmail, ZoneId timeZone) {

        Care care = getCare(careId);
        assertLoggedInUserIsCaretaker(care.getCaretaker().getEmail(), userEmail, "Only caretaker can edit care");
        assertCareIsNotTerminated(care);
        assertCaretakerStatusIsPending(care, "Cannot update accepted care");
        assertEndCareDateIsAfterStartCareDate(updateCare.careStart(), updateCare.careEnd());
        careMapper.updateCareFromDTO(updateCare, care);
        care.setClientStatus(CareStatus.PENDING);
        Care savedCare = careRepository.save(care);
        notificationService.addNotificationForClientAndSend(
                savedCare.getId(),
                ObjectType.CARE,
                savedCare.getClient(),
                "message.care.update_reservation",
                Set.of(care.getCaretaker().getEmail())
        );
        return careMapper.mapToCareDTO(savedCare, timeZone);

    }

    public CareDTO acceptCareByCaretaker(Long careId, String userEmail, ZoneId timeZone) {

        Care care = getCare(careId);
        assertLoggedInUserIsCaretaker(care.getCaretaker().getEmail(), userEmail,
                "Only caretaker can change caretaker status");
        assertCareIsNotTerminated(care);
        assertCaretakerStatusIsPending(care, "Care already accepted");
        assertClientStatusIsAccepted(care);
        care.setCaretakerStatus(CareStatus.AWAITING_PAYMENT);
        care.setClientStatus(CareStatus.AWAITING_PAYMENT);
        Care savedCare = careRepository.save(care);
        notificationService.addNotificationForClientAndSend(
                savedCare.getId(),
                ObjectType.CARE,
                savedCare.getClient(),
                "message.care.accepted_reservation",
                Set.of(care.getCaretaker().getEmail())
        );
        return careMapper.mapToCareDTO(savedCare, timeZone);
    }

    public CareDTO acceptCareByClient(Long careId, String userEmail, ZoneId timeZone) {

        Care care =  getCare(careId);
        assertLoggedInUserIsClient(care.getClient().getEmail(), userEmail, "Only client can change client status");
        assertCareIsNotTerminated(care);
        assertClientStatusIsPending(care, "Care already accepted");
        care.setClientStatus(CareStatus.ACCEPTED);
        Care savedCare = careRepository.save(care);
        notificationService.addNotificationForCaretakerAndSend(
                savedCare.getId(),
                ObjectType.CARE,
                savedCare.getCaretaker(),
                "message.care.accepted_reservation",
                Set.of(care.getClient().getEmail())
        );
        return careMapper.mapToCareDTO(savedCare, timeZone);
    }

    public CareDTO rejectCareByCaretaker(Long careId, String userEmail, ZoneId timeZone) {

        Care care = getCare(careId);
        assertLoggedInUserIsCaretaker(care.getCaretaker().getEmail(), userEmail,
                "Only caretaker can change caretaker status");
        assertCareIsNotTerminated(care);
        assertCaretakerStatusIsPending(care, "Cannot reject already accepted care");
        care.setCaretakerStatus(CareStatus.CANCELLED);
        Care savedCare = careRepository.save(care);
        notificationService.addNotificationForClientAndSend(
                savedCare.getId(),
                ObjectType.CARE,
                savedCare.getClient(),
                "message.care.rejected_reservation",
                Set.of(care.getCaretaker().getEmail())
        );
        return careMapper.mapToCareDTO(savedCare, timeZone);

    }

    public CareDTO cancelCareByClient(Long careId, String userEmail, ZoneId timeZone) {

        Care care = getCare(careId);
        assertLoggedInUserIsClient(care.getClient().getEmail(), userEmail,
                "Only client can change client status");
        assertCareIsNotTerminated(care);
        assertCaretakerStatusIsPending(care, "Cannot cancel care accepted by caretaker");
        care.setClientStatus(CareStatus.CANCELLED);
        Care savedCare = careRepository.save(care);
        notificationService.addNotificationForCaretakerAndSend(
                savedCare.getId(),
                ObjectType.CARE,
                savedCare.getCaretaker(),
                "message.care.rejected_reservation",
                Set.of(care.getClient().getEmail())
        );
        return careMapper.mapToCareDTO(savedCare, timeZone);

    }

    public Page<CareDTO> getCares(Pageable pageable, CareSearchCriteria filters, Set<String> emails,
                                  String userEmail, Role selectedProfile, ZoneId zoneId) {

        Specification<Care> spec = selectedProfile == Role.CARETAKER
                ? CareSpecificationUtils.toSpecificationForCaretaker(filters, emails, userEmail)
                : CareSpecificationUtils.toSpecificationForClient(filters, emails, userEmail);

        return careRepository.findAll(spec, pageable)
                .map(care -> careMapper.mapToCareDTO(care, zoneId));

    }

    private Care createCare(CreateCareDTO createCare, Set<AnimalAttribute> animalAttributes) {

        return Care.builder()
                .caretakerStatus(CareStatus.PENDING)
                .clientStatus(CareStatus.ACCEPTED)
                .careStart(createCare.careStart())
                .careEnd(createCare.careEnd())
                .description(createCare.description())
                .dailyPrice(createCare.dailyPrice())
                .animal(animalService.getAnimal(createCare.animalType()))
                .animalAttributes(animalAttributes)
                .caretaker(caretakerService.getCaretakerByEmail(createCare.caretakerEmail()))
                .client(clientService.getClientByEmail(createCare.clientEmail()))
                .build();

    }

    private void assertLoggedInUserIsClient(String clientEmail,
                                            String userEmail, String message) {

        if(!clientEmail.equals(userEmail)) {
            throw new IllegalActionException(message);
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

    private void assertCareIsNotTerminated(Care care) {
        assertCareNotCancelled(care);
        assertCareNotOutdated(care);
    }

    private void assertCareNotOutdated(Care care) {
        if(care.getClientStatus().equals(CareStatus.OUTDATED) || care.getCaretakerStatus().equals(CareStatus.OUTDATED)) {
            throw new IllegalActionException("Care already outdated");
        }
    }

    private void assertCareNotCancelled(Care care) {
        if(care.getClientStatus().equals(CareStatus.CANCELLED) || care.getCaretakerStatus().equals(CareStatus.CANCELLED)) {
            throw new IllegalActionException("Care already cancelled");
        }
    }

    private void assertCaretakerStatusIsPending(Care care, String message) {
        if(!care.getCaretakerStatus().equals(CareStatus.PENDING)) {
            throw new IllegalActionException(message);
        }
    }

    private void assertClientStatusIsPending(Care care, String message) {
        if(!care.getClientStatus().equals(CareStatus.PENDING)) {
            throw new IllegalActionException(message);
        }
    }

    private void assertLoggedInUserIsCaretaker(String caretakerEmail, String userEmail, String message) {
        if(!caretakerEmail.equals(userEmail)) {
            throw new IllegalActionException(message);
        }
    }

    private Care getCare(Long careId) {
        return careRepository.findById(careId)
                .orElseThrow(() -> new NotFoundException("Care not found"));
    }

    private void assertClientStatusIsAccepted(Care care) {
        if(!care.getClientStatus().equals(CareStatus.ACCEPTED)) {
            throw new IllegalActionException("Client need to first accept care");
        }
    }

}
