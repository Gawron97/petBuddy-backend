package com.example.petbuddybackend.config.geolocation;

import com.opencagedata.jopencage.JOpenCageGeocoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeocodingConfig {

    @Value("${opencage.api.key}")
    private String openCageApiKey;

    @Bean
    public JOpenCageGeocoder jOpenCageGeocoder() {
        return new JOpenCageGeocoder(openCageApiKey);
    }

}
