package com.example.petbuddybackend.service.care;

import com.example.petbuddybackend.dto.care.CreateCareDTO;
import com.example.petbuddybackend.dto.care.DetailedCareDTO;
import com.example.petbuddybackend.dto.care.UpdateCareDTO;
import com.example.petbuddybackend.dto.criteriaSearch.CareSearchCriteria;
import com.example.petbuddybackend.dto.user.SimplifiedAccountDataDTO;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.notification.ObjectType;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.repository.care.CareRepository;
import com.example.petbuddybackend.service.animal.AnimalService;
import com.example.petbuddybackend.service.block.BlockService;
import com.example.petbuddybackend.service.care.state.CareStateMachine;
import com.example.petbuddybackend.service.mapper.CareMapper;
import com.example.petbuddybackend.service.notification.NotificationService;
import com.example.petbuddybackend.service.user.CaretakerService;
import com.example.petbuddybackend.service.user.ClientService;
import com.example.petbuddybackend.service.user.UserService;
import com.example.petbuddybackend.utils.exception.throweable.general.ForbiddenException;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.specification.CareSpecificationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CareService {

    private static final String CARE = "Care";
    private static final String CARETAKER_NOT_OWNER_MESSAGE = "Caretaker is not owner of the care";
    private static final String CLIENT_NOT_OWNER_MESSAGE = "Client is not owner of the care";

    @Value("${notification.care.reservation}")
    private String CREATE_RESERVATION_MESSAGE;

    @Value("${notification.care.update_reservation}")
    private String UPDATE_RESERVATION_MESSAGE;

    @Value("${notification.care.accepted_reservation}")
    private String ACCEPT_RESERVATION_MESSAGE;

    @Value("${notification.care.rejected_reservation}")
    private String REJECT_RESERVATION_MESSAGE;

    private final CareRepository careRepository;
    private final AnimalService animalService;
    private final CaretakerService caretakerService;
    private final ClientService clientService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final CareMapper careMapper = CareMapper.INSTANCE;
    private final CareStateMachine careStateMachine;
    private final BlockService blockService;

    public DetailedCareDTO makeReservation(CreateCareDTO createCare, String clientEmail, String caretakerEmail,
                                   ZoneId timeZone) {
        userService.assertHasRole(clientEmail, Role.CLIENT);
        userService.assertHasRole(caretakerEmail, Role.CARETAKER);
        assertNotReservationToYourself(clientEmail, caretakerEmail);
        blockService.assertNotBlockedByAny(clientEmail, caretakerEmail);

        Set<AnimalAttribute> animalAttributes = animalService.getAnimalAttributes(
                createCare.animalType(), createCare.selectedOptions()
        );

        Care care = careRepository.save(
                createCareFromReservation(clientEmail, caretakerEmail, createCare, animalAttributes)
        );

        sendCaretakerCareNotification(care, CREATE_RESERVATION_MESSAGE);
        return careMapper.mapToDetailedCareDTO(care, timeZone);
    }

    public DetailedCareDTO updateCare(Long careId, UpdateCareDTO updateCare, String caretakerEmail, ZoneId timeZone) {
        Care care = getCareOfCaretaker(careId, caretakerEmail);

        careStateMachine.transitionToEditCare(care);
        careMapper.updateCareFromDTO(updateCare, care);

        Care savedCare = careRepository.save(care);
        sendClientCareNotification(savedCare, UPDATE_RESERVATION_MESSAGE);
        return careMapper.mapToDetailedCareDTO(savedCare, timeZone);
    }

    public DetailedCareDTO clientChangeCareStatus(Long careId, String clientEmail, ZoneId timeZone,
                                                  CareStatus newStatus) {
        userService.assertHasRole(clientEmail, Role.CLIENT);
        Care care = getCareOfClient(careId, clientEmail);

        careStateMachine.transition(Role.CLIENT, care, newStatus);
        care = careRepository.save(care);
        sendCaretakerCareNotification(care, getNotificationOnStatusChange(newStatus));

        return careMapper.mapToDetailedCareDTO(care, timeZone);
    }

    public DetailedCareDTO caretakerChangeCareStatus(Long careId, String caretakerEmail, ZoneId timeZone,
                                             CareStatus newStatus) {
        userService.assertHasRole(caretakerEmail, Role.CARETAKER);
        Care care = getCareOfCaretaker(careId, caretakerEmail);

        careStateMachine.transition(Role.CARETAKER, care, newStatus);
        care = careRepository.save(care);
        sendClientCareNotification(care, getNotificationOnStatusChange(newStatus));

        return careMapper.mapToDetailedCareDTO(care, timeZone);
    }


    public Page<DetailedCareDTO> getCares(Pageable pageable, CareSearchCriteria filters, Set<String> emails,
                                  String userEmail, Role selectedProfile, ZoneId zoneId) {

        Specification<Care> spec = selectedProfile == Role.CARETAKER
                ? CareSpecificationUtils.toSpecificationForCaretaker(filters, emails, userEmail)
                : CareSpecificationUtils.toSpecificationForClient(filters, emails, userEmail);

        return careRepository.findAll(spec, pageable)
                .map(care -> careMapper.mapToDetailedCareDTO(care, zoneId));

    }

    public Care getCareById(Long careId) {
        return careRepository.findById(careId)
                .orElseThrow(() -> NotFoundException.withFormattedMessage(CARE, careId.toString()));
    }

    public Page<SimplifiedAccountDataDTO> getUsersRelatedToYourCares(String userEmail, Pageable pageable, Role role) {
        return role == Role.CARETAKER
        ? careRepository.findClientsRelatedToYourCares(userEmail, pageable)
        : careRepository.findCaretakersRelatedToYourCares(userEmail, pageable);
    }

    public DetailedCareDTO getCare(Long careId, ZoneId timezone, String userEmail) {
        Care care = getCareById(careId);
        assertUserParticipatingInCare(care, userEmail);
        return careMapper.mapToDetailedCareDTO(care, timezone);
    }

    public DetailedCareDTO markCareAsConfirmed(Long careId, String name, Role role, ZoneId orSystemDefault) {
        Care care = getCareById(careId);
        careStateMachine.transition(role, care, CareStatus.CONFIRMED);
        Care savedCare = careRepository.save(care);
        return careMapper.mapToDetailedCareDTO(savedCare, orSystemDefault);
    }

    private void assertNotReservationToYourself(String clientEmail, String caretakerEmail) {
        if(clientEmail.equals(caretakerEmail)) {
            throw new IllegalActionException("Cannot make reservation to yourself");
        }
    }

    private String getNotificationOnStatusChange(CareStatus status) {
        return switch(status) {
            case ACCEPTED -> ACCEPT_RESERVATION_MESSAGE;
            case CANCELLED -> REJECT_RESERVATION_MESSAGE;
            default -> throw new UnsupportedOperationException("Unsupported status: " + status.name());
        };
    }

    private void sendClientCareNotification(Care care, String message) {
        Client client = care.getClient();

        notificationService.addNotificationForClientAndSend(
                care.getId(), ObjectType.CARE, client, message, Set.of(client.getEmail())
        );
    }

    private void sendCaretakerCareNotification(Care care, String message) {
        Caretaker caretaker = care.getCaretaker();

        notificationService.addNotificationForCaretakerAndSend(
                care.getId(), ObjectType.CARE, caretaker, message, Set.of(caretaker.getEmail())
        );
    }

    private Care createCareFromReservation(
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

    /**
     * Get care of id and check if the caretakerEmail is owner o the returned care
     * */
    private Care getCareOfCaretaker(Long careId, String caretakerEmail) {
        Care care = getCareById(careId);

        if(!care.getCaretaker().getEmail().equals(caretakerEmail)) {
            throw new IllegalActionException(CARETAKER_NOT_OWNER_MESSAGE);
        }

        return care;
    }

    /**
     * Get care of id and check if the clientEmail is the one issuing the care
     * */
    private Care getCareOfClient(Long careId, String clientEmail) {
        Care care = getCareById(careId);

        if (!care.getClient().getEmail().equals(clientEmail)) {
            throw new IllegalActionException(CLIENT_NOT_OWNER_MESSAGE);
        }

        return care;
    }

    private void assertUserParticipatingInCare(Care care, String userEmail) {
        if (!care.getClient().getEmail().equals(userEmail) && !care.getCaretaker().getEmail().equals(userEmail)) {
            throw new ForbiddenException("User is not participating in the care");
        }
    }
}
