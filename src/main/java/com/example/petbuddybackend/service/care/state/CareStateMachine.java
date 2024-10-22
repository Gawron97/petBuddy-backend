package com.example.petbuddybackend.service.care.state;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.user.Role;
import org.springframework.stereotype.Service;

@Service
public final class CareStateMachine {

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
                this::acceptAsCaretaker, care -> care.getClientStatus().equals(CareStatus.PENDING));

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
