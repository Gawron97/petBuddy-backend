package com.example.petbuddybackend.service.care.state;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.repository.care.CareRepository;
import com.example.petbuddybackend.utils.exception.throweable.StateTransitionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CareStateMachine {

    private static final String ACCEPTED_CARE_EDIT_MESSAGE = "Cannot edit care that has been accepted by caretaker";

    private static final List<CareStatus> globalStatusesThatCanBeCancelled =
            List.of(CareStatus.PENDING, CareStatus.ACCEPTED, CareStatus.AWAITING_PAYMENT);

    private static final List<CareStatus> globalStatusesThatCanBeOutdated =
            List.of(CareStatus.PENDING, CareStatus.ACCEPTED, CareStatus.AWAITING_PAYMENT);

    private final TransitionManager transitionManager = initTransitionManager();
    private final CareRepository careRepository;


    /**
     * Transition state related to role
     * */
    public Care transition(Role role, Care care, CareStatus statusToTransition) {
        return transitionManager.transition(role, care, statusToTransition);
    }

    /**
     * Transition for both roles
     * */
    public Care transitionBothRoles(Care care, CareStatus statusToTransition) {
        return transitionManager.transitionBothRoles(care, statusToTransition);
    }

    public void transitionToEditCare(Care care) {
        CareStatus clientStatus = care.getClientStatus();
        CareStatus caretakerStatus = care.getCaretakerStatus();

        if(caretakerStatus.equals(CareStatus.ACCEPTED)) {
            throw new StateTransitionException(ACCEPTED_CARE_EDIT_MESSAGE);
        }

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
        return careRepository.outdateCaresBetweenClientAndCaretaker(globalStatusesThatCanBeOutdated);
    }

    private TransitionManager initTransitionManager() {
        TransitionManager transitionManager = new TransitionManager();
        initClientTransitions(transitionManager);
        initCaretakerTransitions(transitionManager);
        initGlobalTransitions(transitionManager);
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

        // Pay care
        transitionManager.addTransition(Role.CARETAKER, CareStatus.AWAITING_PAYMENT, CareStatus.PAID, this::setBothStatuses);
    }

    private void initGlobalTransitions(TransitionManager transitionManager) {
        // Obsolete care
        for(CareStatus statusPrerequisite: globalStatusesThatCanBeCancelled) {
            transitionManager.addTransition(statusPrerequisite, CareStatus.CANCELLED, this::setBothStatuses);
        }

        for(CareStatus statusPrerequisite: globalStatusesThatCanBeOutdated) {
            transitionManager.addTransition(statusPrerequisite, CareStatus.OUTDATED, this::setBothStatuses);
        }
    }

    private boolean clientShouldBeAccepted(Care care) {
        return care.getClientStatus().equals(CareStatus.ACCEPTED);
    }

    private void acceptAsClient(Care care, CareStatus noop) {
        care.setClientStatus(CareStatus.ACCEPTED);
        awaitPaymentOnAccept(care);
    }

    private void acceptAsCaretaker(Care care, CareStatus noop) {
        care.setCaretakerStatus(CareStatus.ACCEPTED);
        awaitPaymentOnAccept(care);
    }

    private void awaitPaymentOnAccept(Care care) {
        if(care.getCaretakerStatus() == CareStatus.ACCEPTED && care.getClientStatus() == CareStatus.ACCEPTED) {
            care.setClientStatus(CareStatus.AWAITING_PAYMENT);
            care.setCaretakerStatus(CareStatus.AWAITING_PAYMENT);
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
                globalStatusesThatCanBeCancelled
        );
    }
}
