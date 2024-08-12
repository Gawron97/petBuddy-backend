package com.example.petbuddybackend.entity.animal.keys;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnimalAttributeKey implements Serializable {

    private String animalType;
    private String attributeName;

}
