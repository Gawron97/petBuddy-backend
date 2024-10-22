package com.example.petbuddybackend.service.care;

import com.example.petbuddybackend.dto.care.CareDTO;
import com.example.petbuddybackend.dto.care.CreateCareDTO;
import com.example.petbuddybackend.dto.care.UpdateCareDTO;
import com.example.petbuddybackend.dto.criteriaSearch.CareSearchCriteria;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.notification.ObjectType;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.repository.care.CareRepository;
import com.example.petbuddybackend.service.animal.AnimalService;
import com.example.petbuddybackend.service.care.state.CareStateMachine;
import com.example.petbuddybackend.service.mapper.CareMapper;
import com.example.petbuddybackend.service.notification.NotificationService;
import com.example.petbuddybackend.service.user.CaretakerService;
import com.example.petbuddybackend.service.user.ClientService;
import com.example.petbuddybackend.service.user.UserService;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.specification.CareSpecificationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CareService {

    @Value("${notification.message.reservation}")
    private String reservationMessage;

    @Value("${notification.message.update_reservation}")
    private String updateReservationMessage;

    @Value("${notification.message.accepted_reservation}")
    private String acceptReservationMessage;

    @Value("${notification.message.rejected_reservation}")
    private String rejectReservationMessage;

    private final CareRepository careRepository;
    private final AnimalService animalService;
    private final CaretakerService caretakerService;
    private final ClientService clientService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final CareMapper careMapper = CareMapper.INSTANCE;
    private final CareStateMachine careStateMachine;

    public CareDTO makeReservation(CreateCareDTO createCare, String clientEmail, String caretakerEmail, ZoneId timeZone) {
        userService.assertHasRole(clientEmail, Role.CLIENT);
        userService.assertHasRole(caretakerEmail, Role.CARETAKER);
        assertEndCareDateIsAfterStartCareDate(createCare.careStart(), createCare.careEnd());
        userService.assertNotBlockedByAny(clientEmail, caretakerEmail);

        Set<AnimalAttribute> animalAttributes = animalService.getAnimalAttributes(createCare.animalAttributeIds());
        assertAnimalAttributesMatchAnimalType(animalAttributes, createCare.animalType());

        Care care = careRepository.save(createCare(clientEmail, caretakerEmail, createCare, animalAttributes));
        sendCaretakerCareNotification(care, reservationMessage);
        return careMapper.mapToCareDTO(care, timeZone);
    }

    public CareDTO updateCare(Long careId, UpdateCareDTO updateCare, String caretakerEmail, ZoneId timeZone) {
        Care care = getCareOfCaretaker(careId, caretakerEmail);
        assertCareIsNotTerminated(care);
        assertEndCareDateIsAfterStartCareDate(updateCare.careStart(), updateCare.careEnd());

        careMapper.updateCareFromDTO(updateCare, care);
        care.setClientStatus(CareStatus.PENDING);

        Care savedCare = careRepository.save(care);
        sendClientCareNotification(savedCare, updateReservationMessage);
        return careMapper.mapToCareDTO(savedCare, timeZone);
    }

    public CareDTO clientChangeCareStatus(Long careId, String clientEmail, ZoneId timeZone, CareStatus newStatus) {
        userService.assertHasRole(clientEmail, Role.CLIENT);
        Care care = getCareOfClient(careId, clientEmail);

        CareStatus currentStatus =  care.getClientStatus();

        if(!currentStatus.equals(newStatus)) {
            careStateMachine.transition(care, Role.CLIENT, newStatus);
            care = careRepository.save(care);
            sendCaretakerCareNotification(care, getNotificationOnStatusChange(newStatus));
        }

        return careMapper.mapToCareDTO(care, timeZone);
    }

    public CareDTO caretakerChangeCareStatus(Long careId, String clientEmail, ZoneId timeZone, CareStatus newStatus) {
        userService.assertHasRole(clientEmail, Role.CARETAKER);
        Care care = getCareOfCaretaker(careId, clientEmail);

        CareStatus currentStatus = care.getCaretakerStatus();

        if(!currentStatus.equals(newStatus)) {
            careStateMachine.transition(care, Role.CARETAKER, newStatus);
            care = careRepository.save(care);
            sendClientCareNotification(care, getNotificationOnStatusChange(newStatus));
        }

        return careMapper.mapToCareDTO(care, timeZone);
    }

    public Page<CareDTO> getCares(Pageable pageable, CareSearchCriteria filters, Set<String> emails,
                                  String userEmail, Role selectedProfile, ZoneId zoneId) {

        Specification<Care> spec = selectedProfile == Role.CARETAKER
                ? CareSpecificationUtils.toSpecificationForCaretaker(filters, emails, userEmail)
                : CareSpecificationUtils.toSpecificationForClient(filters, emails, userEmail);

        return careRepository.findAll(spec, pageable)
                .map(care -> careMapper.mapToCareDTO(care, zoneId));

    }

    private String getNotificationOnStatusChange(CareStatus status) {
        return switch(status) {
            case ACCEPTED -> acceptReservationMessage;
            case CANCELLED -> rejectReservationMessage;
            default -> throw new IllegalActionException("Invalid status");
        };
    }

    private void sendClientCareNotification(Care care, String message) {
        Client client = care.getClient();

        notificationService.addNotificationForClientAndSend(
                care.getId(),
                ObjectType.CARE,
                client,
                String.format(message, client.getEmail(), care.getId())
        );
    }

    private void sendCaretakerCareNotification(Care care, String message) {
        Caretaker caretaker = care.getCaretaker();

        notificationService.addNotificationForCaretakerAndSend(
                care.getId(),
                ObjectType.CARE,
                caretaker,
                String.format(message, caretaker.getEmail(), care.getId())
        );
    }

    private Care createCare(
            String clientEmail,
            String caretakerEmail,
            CreateCareDTO createCare,
            Set<AnimalAttribute> animalAttributes
    ) {
        return Care.builder()
                .caretakerStatus(CareStatus.PENDING)
                .clientStatus(CareStatus.ACCEPTED)
                .careStart(createCare.careStart())
                .careEnd(createCare.careEnd())
                .description(createCare.description())
                .dailyPrice(createCare.dailyPrice())
                .animal(animalService.getAnimal(createCare.animalType()))
                .animalAttributes(animalAttributes)
                .client(clientService.getClientByEmail(clientEmail))
                .caretaker(caretakerService.getCaretakerByEmail(caretakerEmail))
                .build();

    }

    private void assertEndCareDateIsAfterStartCareDate(LocalDate careStart, LocalDate careEnd) {
        if(careEnd.isBefore(careStart)) {
            throw new IllegalActionException("End care date must be after start care date");
        }
    }

    private void assertAnimalAttributesMatchAnimalType(Set<AnimalAttribute> animalAttributes, String animalType) {
        List<AnimalAttribute> mismatchedAttributes = animalAttributes.stream()
                .filter(animalAttribute -> !animalAttribute.getAnimal().getAnimalType().equals(animalType))
                .toList();

        if (!mismatchedAttributes.isEmpty()) {
            String mismatches = mismatchedAttributes.stream()
                    .map(animalAttribute -> animalAttribute.getAnimal().getAnimalType() + " (attribute: " + animalAttribute + ")")
                    .collect(Collectors.joining(", "));
            throw new IllegalActionException("Animal attributes must match animal type. Mismatches: " + mismatches);
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

    /**
     * Get care of id and check if the caretakerEmail is owner o the returned care
     * */
    private Care getCareOfCaretaker(Long careId, String caretakerEmail) {
        Care care = careRepository.findById(careId)
                .orElseThrow(() -> NotFoundException.withFormattedMessage("Care", careId.toString()));

        if(!care.getCaretaker().getEmail().equals(caretakerEmail)) {
            throw new IllegalActionException("Caretaker is not owner of the care");
        }

        return care;
    }

    /**
     * Get care of id and check if the clientEmail is the one issuing the care
     * */
    private Care getCareOfClient(Long careId, String clientEmail) {
        Care care = careRepository.findById(careId)
                .orElseThrow(() -> NotFoundException.withFormattedMessage("Care", careId.toString()));

        if(!care.getClient().getEmail().equals(clientEmail)) {
            throw new IllegalActionException("Client is not owner of the care");
        }

        return care;
    }
}
