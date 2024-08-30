package com.example.petbuddybackend.utils.provider.resources;

import com.example.petbuddybackend.utils.resourcestructure.AnimalAttributeConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "animal-attribute-data")
@Getter @Setter
public class AnimalAttributeProvider {

    private List<AnimalAttributeConfig> animalAttributeConfigs;

}
