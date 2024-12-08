package com.example.petbuddybackend.entity.notification;

import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CaretakerNotification extends Notification {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caretakerEmail", nullable = false, updatable = false)
    private Caretaker caretaker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clientTriggerEmail", nullable = false, updatable = false)
    private Client clientTrigger;

}
