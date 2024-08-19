package com.example.petbuddybackend.utils.provider.resources;

import com.example.petbuddybackend.utils.resourceStructure.AnimalConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "animal-data")
@Getter @Setter
public class AnimalProvider {

    private List<AnimalConfig> animalConfigs;

}
