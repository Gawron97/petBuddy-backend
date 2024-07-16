package com.example.petbuddybackend.testutils;

import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.address.PolishVoivodeship;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class MockUtils {
    
    private MockUtils() {}

    public static <T> List<T> createTimes(int times, Supplier<T> supplier) {
        var list = new ArrayList<T>(times);

        for(int i=0; i<times; i++) {
            list.add(supplier.get());
        }

        return list;
    }

    public static AppUser createMockUser() {
        return AppUser.builder()
                .name("name")
                .email("email")
                .surname("surname")
                .build();
    }

    public static Address createMockAddress() {
        return Address.builder()
                .id(1L)
                .city("city")
                .voivodeship(PolishVoivodeship.SLASKIE)
                .street("street")
                .postalCode("postalCode")
                .buildingNumber("buildingNumber")
                .apartmentNumber("apartmentNumber")
                .build();
    }

    public static Caretaker createMockCaretaker() {
        return Caretaker.builder()
                .accountData(createMockUser())
                .address(createMockAddress())
                .description("description")
                .phoneNumber("number")
                .email("email")
                .build();
    }
}
