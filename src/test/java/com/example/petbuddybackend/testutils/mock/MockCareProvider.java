package com.example.petbuddybackend.testutils.mock;

import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashSet;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class MockCareProvider {

    public static Care createMockCare(Caretaker caretaker, Client client, Animal animal) {

        return Care.builder()
                .caretakerStatus(CareStatus.PENDING)
                .clientStatus(CareStatus.ACCEPTED)
                .careStart(LocalDate.now().plusDays(2))
                .careEnd(LocalDate.now().plusDays(7))
                .description("Test care description")
                .dailyPrice(new BigDecimal("50.00"))
                .animal(animal)
                .animalAttributes(new HashSet<>())
                .caretaker(caretaker)
                .client(client)
                .build();

    }

    public static Care createMockCare(Caretaker caretaker, Client client, Animal animal, ZonedDateTime submittedAt,
                                      LocalDate careStart, LocalDate careEnd, BigDecimal dailyPrice,
                                      CareStatus caretakerStatus, CareStatus clientStatus) {

        return Care.builder()
                .submittedAt(submittedAt)
                .caretakerStatus(caretakerStatus)
                .clientStatus(clientStatus)
                .careStart(careStart)
                .careEnd(careEnd)
                .description("Test care description")
                .dailyPrice(dailyPrice)
                .animal(animal)
                .animalAttributes(new HashSet<>())
                .caretaker(caretaker)
                .client(client)
                .build();

    }

}
