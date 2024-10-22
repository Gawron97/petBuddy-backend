package com.example.petbuddybackend.utils.exception.throweable;

import com.example.petbuddybackend.service.care.state.Transition;
import lombok.Getter;


@Getter
public class StateTransitionException extends RuntimeException {

    private static final String STATE_TRANSITION_MESSAGE =
            "Invalid care state transition from %s to %s, triggered by %s";

    public StateTransitionException(Transition transition) {
        super(String.format(
                STATE_TRANSITION_MESSAGE,
                transition.fromStatus().name(),
                transition.toStatus().name(),
                transition.role().name())
        );
    }
}
