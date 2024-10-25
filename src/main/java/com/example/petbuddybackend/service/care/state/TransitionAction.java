package com.example.petbuddybackend.service.care.state;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

@Builder
@Getter @Setter
@AllArgsConstructor
public class TransitionAction {

    private final Predicate<Care> prerequisite;
    private final BiConsumer<Care, CareStatus> onSuccess;

    public TransitionAction(BiConsumer<Care, CareStatus> onSuccess) {
        this.prerequisite = c -> true;
        this.onSuccess = onSuccess;
    }
}
