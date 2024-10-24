package com.example.petbuddybackend.utils.exception.throweable;

import com.example.petbuddybackend.service.care.state.RoleTransition;
import com.example.petbuddybackend.service.care.state.Transition;
import lombok.Getter;

import java.util.List;


@Getter
public class StateTransitionException extends RuntimeException {

    private static final String STATE_TRANSITION_MESSAGE_ROLE =
            "Invalid care state transition from %s to %s, triggered by %s";

    private static final String STATE_TRANSITION_MESSAGE =
            "Invalid care state transition from %s to %s";

    public StateTransitionException(RoleTransition invalidTransition) {
        super(String.format(
                STATE_TRANSITION_MESSAGE_ROLE,
                invalidTransition.getFromStatus().name(),
                invalidTransition.getToStatus().name(),
                invalidTransition.getRole().name())
        );
    }

    public StateTransitionException(List<Transition> invalidTransitions) {
        super(getTransitionMessages(invalidTransitions));
    }

    public StateTransitionException(String message) {
        super(message);
    }

    private static String getTransitionMessages(List<Transition> invalidTransitions) {
        StringBuilder message = new StringBuilder();
        for (Transition transition : invalidTransitions) {
            message.append(String.format(
                    STATE_TRANSITION_MESSAGE,
                    transition.getFromStatus().name(),
                    transition.getToStatus().name())
            );
            message.append("\n");
        }
        return message.toString();
    }
}
