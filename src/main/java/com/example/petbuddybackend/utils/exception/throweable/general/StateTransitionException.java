package com.example.petbuddybackend.utils.exception.throweable.general;

import com.example.petbuddybackend.service.care.state.RoleTransition;
import com.example.petbuddybackend.service.care.state.Transition;
import com.example.petbuddybackend.utils.exception.throweable.HttpException;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.List;


@Getter
public class StateTransitionException extends HttpException {

    private static final HttpStatusCode HTTP_CODE = HttpStatus.BAD_REQUEST;

    private static final String STATE_TRANSITION_MESSAGE_ROLE =
            "Invalid care state transition from %s to %s, triggered by %s";

    private static final String STATE_TRANSITION_MESSAGE =
            "Invalid care state transition from %s to %s";

    public StateTransitionException(RoleTransition invalidTransition) {
        super(createTransitionMessage(invalidTransition), HTTP_CODE);
    }

    public StateTransitionException(List<Transition> invalidTransitions) {
        super(createTransitionMessage(invalidTransitions), HTTP_CODE);
    }

    public StateTransitionException(String message) {
        super(message, HTTP_CODE);
    }

    private static String createTransitionMessage(List<Transition> invalidTransitions) {
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

    private static String createTransitionMessage(RoleTransition transition) {
        return String.format(
                STATE_TRANSITION_MESSAGE_ROLE,
                transition.getFromStatus().name(),
                transition.getToStatus().name(),
                transition.getRole().name()
        );
    }
}
