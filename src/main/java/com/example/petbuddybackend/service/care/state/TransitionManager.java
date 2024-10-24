package com.example.petbuddybackend.service.care.state;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.utils.exception.throweable.StateTransitionException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class TransitionManager {

    private final Map<Transition, TransitionAction> transitions;

    public TransitionManager() {
        transitions = new HashMap<>();
    }

    public void addTransition(Role role, CareStatus fromStatus, CareStatus toStatus, BiConsumer<Care, CareStatus> onSuccess) {
        transitions.put(new RoleTransition(role, fromStatus, toStatus), new TransitionAction(onSuccess));
    }

    public void addTransition(CareStatus fromStatus, CareStatus toStatus, BiConsumer<Care, CareStatus> onSuccess) {
        transitions.put(new Transition(fromStatus, toStatus), new TransitionAction(onSuccess));
    }

    public void addTransition(
            Role role,
            CareStatus fromStatus,
            CareStatus toStatus,
            BiConsumer<Care, CareStatus> onSuccess,
            Predicate<Care> prerequisite
    ) {
        transitions.put(new RoleTransition(role, fromStatus, toStatus), new TransitionAction(prerequisite, onSuccess));
    }

    /**
     * Transition state related to role
     * */
    public Care transition(Role role, Care care, CareStatus newState) {
        Transition transition = role.equals(Role.CARETAKER) ?
                new RoleTransition(role, care.getCaretakerStatus(), newState) :
                new RoleTransition(role, care.getClientStatus(), newState);

        return performTransitions(care, List.of(transition));
    }

    /**
     * Transition for both roles
     * */
    public Care transitionBothRoles(Care care, CareStatus newState) {
        Transition clientTransition = new Transition(care.getClientStatus(), newState);
        Transition caretakerTransition = new Transition(care.getCaretakerStatus(), newState);
        return performTransitions(care, List.of(clientTransition, caretakerTransition));
    }

    private Care performTransitions(Care care, List<Transition> transitions) {
        if(transitions.stream().anyMatch(t -> !this.transitions.containsKey(t))) {
            throw new StateTransitionException(transitions);
        }

        List<TransitionAction> transitionActions = transitions.stream()
                .map(this.transitions::get)
                .toList();

        if(transitionActions.stream().anyMatch(t -> !t.getPrerequisite().test(care))) {
            throw new StateTransitionException(transitions);
        }

        transitionActions.forEach(t -> t.getOnSuccess().accept(care, transitions.get(0).getToStatus()));
        return care;
    }
}