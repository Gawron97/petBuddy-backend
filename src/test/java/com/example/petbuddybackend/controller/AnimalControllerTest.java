package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.service.animal.AnimalService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.HashSet;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AnimalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AnimalService animalService;

    @Test
    void getAnimalAttributes_shouldReturnProperAnswer() throws Exception {

        when(animalService.getAnimalAttributesOfAnimal("dog")).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/animal/attributes/{animalType}", "dog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());

    }

    @Test
    void getAnimals_shouldReturnProperAnswer() throws Exception {

        when(animalService.getAnimals()).thenReturn(new HashSet<>());

        mockMvc.perform(get("/api/animal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

    }

    @Test
    void getAnimalAmenities_shouldReturnProperAnswer() throws Exception {

        when(animalService.getAmenitiesForAnimal("dog")).thenReturn(new HashSet<>());

        mockMvc.perform(get("/api/animal/amenities/{animalType}", "dog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

    }

}
