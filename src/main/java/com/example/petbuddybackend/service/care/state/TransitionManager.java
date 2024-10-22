package com.example.petbuddybackend.service.care.state;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.utils.exception.throweable.StateTransitionException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class TransitionManager {

    private final Map<Transition, TransitionAction> transitions;

    public TransitionManager() {
        transitions = new HashMap<>();
    }

    public void addTransition(Role role, CareStatus fromStatus, CareStatus toStatus, BiConsumer<Care, CareStatus> onSuccess) {
        transitions.put(new Transition(role, fromStatus, toStatus), new TransitionAction(onSuccess));
    }

    public void addTransition(
            Role role,
            CareStatus fromStatus,
            CareStatus toStatus,
            BiConsumer<Care, CareStatus> onSuccess,
            Predicate<Care> prerequisite
    ) {
        transitions.put(new Transition(role, fromStatus, toStatus), new TransitionAction(prerequisite, onSuccess));
    }

    public Care transition(Care care, Role role, CareStatus newState) {
        Transition transition = role.equals(Role.CARETAKER) ?
                new Transition(role, care.getCaretakerStatus(), newState) :
                new Transition(role, care.getClientStatus(), newState);

        if(!transitions.containsKey(transition)) {
            throw new StateTransitionException(transition);
        }

        TransitionAction transitionAction = transitions.get(transition);

        if(!transitionAction.getPrerequisite().test(care)) {
            throw new StateTransitionException(transition);
        }

        transitionAction.getOnSuccess().accept(care, newState);
        return care;
    }
}