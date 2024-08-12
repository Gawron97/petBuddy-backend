package com.example.petbuddybackend.entity.facility;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Facility {

    @Id
    private String facility;

    @OneToMany(mappedBy = "facility", fetch = FetchType.LAZY)
    private List<AnimalFacility> animalFacilities;

}
