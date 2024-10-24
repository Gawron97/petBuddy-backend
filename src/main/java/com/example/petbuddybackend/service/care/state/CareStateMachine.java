package com.example.petbuddybackend.service.care.state;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.utils.exception.throweable.StateTransitionException;
import org.springframework.stereotype.Service;

@Service
public final class CareStateMachine {

    public static final String ACCEPTED_CARE_EDIT_MESSAGE = "Cannot edit care that has been accepted by caretaker";
    private final TransitionManager transitionManager;

    public CareStateMachine() {
        this.transitionManager = new TransitionManager();
        initClientTransitions(transitionManager);
        initCaretakerTransitions(transitionManager);
        initGlobalTransitions(transitionManager);
    }

    public Care transition(Care care, Role role, CareStatus statusToTransition) {
        return transitionManager.transition(care, role, statusToTransition);
    }

    public void transitionToEditCare(Care care) {
        CareStatus clientStatus = care.getClientStatus();
        CareStatus caretakerStatus = care.getCaretakerStatus();

        if(caretakerStatus.equals(CareStatus.ACCEPTED)) {
            throw new StateTransitionException(ACCEPTED_CARE_EDIT_MESSAGE);
        }

        if(!caretakerStatus.equals(CareStatus.PENDING)) {
            throw new StateTransitionException(new Transition(Role.CARETAKER, caretakerStatus, CareStatus.PENDING));
        }

        if(!(clientStatus.equals(CareStatus.ACCEPTED) || clientStatus.equals(CareStatus.PENDING))) {
            throw new StateTransitionException(new Transition(Role.CLIENT, clientStatus, CareStatus.PENDING));
        }

        setBothStatuses(care, CareStatus.PENDING);
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
        transitionManager.addTransition(null, CareStatus.PENDING, CareStatus.OUTDATED, this::setBothStatuses);
        transitionManager.addTransition(null, CareStatus.ACCEPTED, CareStatus.OUTDATED, this::setBothStatuses);
        transitionManager.addTransition(null, CareStatus.AWAITING_PAYMENT, CareStatus.OUTDATED, this::setBothStatuses);
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
        if(care.getCaretakerStatus() == CareStatus.ACCEPTED) {
            care.setClientStatus(CareStatus.AWAITING_PAYMENT);
            care.setCaretakerStatus(CareStatus.AWAITING_PAYMENT);
        }
    }

    private void setBothStatuses(Care care, CareStatus newStatus) {
        care.setClientStatus(newStatus);
        care.setCaretakerStatus(newStatus);
    }
}
