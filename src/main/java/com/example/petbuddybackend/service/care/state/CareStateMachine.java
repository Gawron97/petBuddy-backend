package com.example.petbuddybackend.service.care.state;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.repository.care.CareRepository;
import com.example.petbuddybackend.utils.exception.throweable.general.StateTransitionException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CareStateMachine {

    private static final List<CareStatus> statusesThatCanBeCancelledBySystem =
            List.of(CareStatus.PENDING, CareStatus.ACCEPTED);

    private static final List<CareStatus> statusesThatCanBeOutdatedBySystem =
            List.of(CareStatus.PENDING, CareStatus.ACCEPTED, CareStatus.READY_TO_PROCEED);

    private static final List<CareStatus> globalStatusesThatCanBeRated =
            List.of(CareStatus.READY_TO_PROCEED, CareStatus.CONFIRMED);

    @Value("${care.accept-time-window}")
    private Duration CONFIRM_CARE_TIME_WINDOW;

    private final TransitionManager transitionManager = initTransitionManager();
    private final CareRepository careRepository;


    /**
     * Transition state related to role
     * */
    public Care transition(Role role, Care care, CareStatus statusToTransition) {
        return transitionManager.transition(role, care, statusToTransition);
    }

    public void transitionToEditCare(Care care) {
        CareStatus clientStatus = care.getClientStatus();
        CareStatus caretakerStatus = care.getCaretakerStatus();

        if(!caretakerStatus.equals(CareStatus.PENDING)) {
            throw new StateTransitionException(new RoleTransition(Role.CARETAKER, caretakerStatus, CareStatus.PENDING));
        }

        if(!(clientStatus.equals(CareStatus.ACCEPTED) || clientStatus.equals(CareStatus.PENDING))) {
            throw new StateTransitionException(new RoleTransition(Role.CLIENT, clientStatus, CareStatus.PENDING));
        }

        care.setCaretakerStatus(CareStatus.ACCEPTED);
        care.setClientStatus(CareStatus.PENDING);
    }

    @Transactional
    public void cancelCaresIfStatePermitsAndSave(String firstUsername, String secondUsername) {
        cancelCaresOfClientAndCaretakerAndSave(firstUsername, secondUsername);
        cancelCaresOfClientAndCaretakerAndSave(secondUsername, firstUsername);
    }

    @Transactional
    public int outdateCaresIfStatePermitsAndSave() {
        return careRepository.outdateCaresBetweenClientAndCaretaker(
                statusesThatCanBeOutdatedBySystem,
                LocalDate.now().minusDays(CONFIRM_CARE_TIME_WINDOW.toDays())
        );
    }

    public boolean canBeRated(Care care) {
        return globalStatusesThatCanBeRated.contains(care.getClientStatus()) &&
                globalStatusesThatCanBeRated.contains(care.getCaretakerStatus());
    }

    private TransitionManager initTransitionManager() {
        TransitionManager transitionManager = new TransitionManager();
        initClientTransitions(transitionManager);
        initCaretakerTransitions(transitionManager);
        return transitionManager;
    }

    private void initClientTransitions(TransitionManager transitionManager) {
        // Accept care
        transitionManager.addTransition(Role.CLIENT, CareStatus.PENDING, CareStatus.ACCEPTED, this::acceptAsClient);

        // Decline care
        transitionManager.addTransition(Role.CLIENT, CareStatus.PENDING, CareStatus.CANCELLED, this::setBothStatuses);
        transitionManager.addTransition(Role.CLIENT, CareStatus.ACCEPTED, CareStatus.CANCELLED, this::setBothStatuses);
    }

    private void initCaretakerTransitions(TransitionManager transitionManager) {
        // Accept care
        transitionManager.addTransition(Role.CARETAKER, CareStatus.PENDING, CareStatus.ACCEPTED,
                this::acceptAsCaretaker, this::clientShouldBeAccepted);

        // Decline care
        transitionManager.addTransition(Role.CARETAKER, CareStatus.PENDING, CareStatus.CANCELLED, this::setBothStatuses);
        transitionManager.addTransition(Role.CARETAKER, CareStatus.ACCEPTED, CareStatus.CANCELLED, this::setBothStatuses);

        // Accept care
        transitionManager.addTransition(Role.CARETAKER, CareStatus.READY_TO_PROCEED, CareStatus.CONFIRMED,
                this::setBothStatuses, this::careWithinAcceptTimeWindow);
    }

    private boolean clientShouldBeAccepted(Care care) {
        return care.getClientStatus().equals(CareStatus.ACCEPTED);
    }

    private boolean careWithinAcceptTimeWindow(Care care) {
        LocalDate now = LocalDate.now();

        // Care did not start yet
        if(care.getCareStart().isAfter(now)) {
            return false;
        }

        // Check if care can be accepted within time window after care start
        LocalDate acceptThreshold = care.getCareStart()
                .plusDays(CONFIRM_CARE_TIME_WINDOW.toDays());

        return !now.isAfter(acceptThreshold);
    }

    private void acceptAsClient(Care care, CareStatus noop) {
        care.setClientStatus(CareStatus.ACCEPTED);
        transitionToReadyToProceedOnAccept(care);
    }

    private void acceptAsCaretaker(Care care, CareStatus noop) {
        care.setCaretakerStatus(CareStatus.ACCEPTED);
        transitionToReadyToProceedOnAccept(care);
    }

    private void transitionToReadyToProceedOnAccept(Care care) {
        if(care.getCaretakerStatus() == CareStatus.ACCEPTED && care.getClientStatus() == CareStatus.ACCEPTED) {
            care.setClientStatus(CareStatus.READY_TO_PROCEED);
            care.setCaretakerStatus(CareStatus.READY_TO_PROCEED);
        }
    }

    private void setBothStatuses(Care care, CareStatus newStatus) {
        care.setClientStatus(newStatus);
        care.setCaretakerStatus(newStatus);
    }

    private void cancelCaresOfClientAndCaretakerAndSave(String clientEmail, String caretakerEmail) {
        careRepository.cancelCaresBetweenClientAndCaretaker(
                clientEmail,
                caretakerEmail,
                statusesThatCanBeCancelledBySystem
        );
    }
}
