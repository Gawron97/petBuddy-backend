package com.example.petbuddybackend.utils.provider.resources;

import com.example.petbuddybackend.utils.resourceStructure.AnimalAmenityConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "animal-amenity-data")
@Getter
@Setter
public class AnimalAmenityProvider {

    private List<AnimalAmenityConfig> animalAmenityConfigs;

}
