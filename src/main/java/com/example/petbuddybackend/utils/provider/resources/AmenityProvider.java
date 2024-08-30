package com.example.petbuddybackend.utils.provider.resources;

import com.example.petbuddybackend.utils.resourcestructure.AmenityConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "amenity-data")
@Getter
@Setter
public class AmenityProvider {

    private List<AmenityConfig> amenityConfigs;

}
