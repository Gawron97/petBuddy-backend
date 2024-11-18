package com.example.petbuddybackend.utils.provider.geolocation;

import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.provider.geolocation.dto.Coordinates;
import com.opencagedata.jopencage.JOpenCageGeocoder;
import com.opencagedata.jopencage.model.JOpenCageForwardRequest;
import com.opencagedata.jopencage.model.JOpenCageResponse;
import com.opencagedata.jopencage.model.JOpenCageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class GeolocationProvider {

    private static final String ADDRESS_FORMAT = "%s, %s, %s";
    private static final String GEOLOCATION = "Geolocation";

    private final JOpenCageGeocoder jOpenCageGeocoder;

    private static Long lastRequestTime = 1_577_833_200_000L;

    public Coordinates getCoordinatesOfAddress(String country, String city) {
        return getCoordinatesOfAddress(country, city, null);
    }

    public Coordinates getCoordinatesOfAddress(String country, String city, String streetName) {
        String formattedAddress = formatAddress(country, city, streetName);
        JOpenCageForwardRequest request = buildRequest(formattedAddress);
        JOpenCageResponse response = sendRequest(request);
        assertResultExists(response, formattedAddress);
        return unpackResultToCoordinates(response);
    }

    private JOpenCageForwardRequest buildRequest(String formattedAddress) {
        JOpenCageForwardRequest request = new JOpenCageForwardRequest(formattedAddress);
        request.setLanguage("pl");
        request.setLimit(1);

        return request;
    }

    private String formatAddress(String country, String city, String streetName) {
        return String.format(ADDRESS_FORMAT, streetName, city, country);
    }

    private JOpenCageResponse sendRequest(JOpenCageForwardRequest request) {
        waitToRequestIfNeeded();
        lastRequestTime = System.currentTimeMillis();
        return jOpenCageGeocoder.forward(request);
    }

    private void waitToRequestIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastRequestTime < 1000) {
            try {
                Thread.sleep(1000 - (currentTime - lastRequestTime));
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Coordinates unpackResultToCoordinates(JOpenCageResponse response) {
        JOpenCageResult result = response.getResults().get(0);
        return Coordinates.builder()
                .latitude(BigDecimal.valueOf(result.getGeometry().getLat()).setScale(4, RoundingMode.HALF_UP))
                .longitude(BigDecimal.valueOf(result.getGeometry().getLng()).setScale(4, RoundingMode.HALF_UP))
                .build();
    }

    private void assertResultExists(JOpenCageResponse response, String formattedAddress) {
        if(response.getResults() == null || response.getResults().isEmpty()) {
            throw NotFoundException.withFormattedMessage(GEOLOCATION, formattedAddress);
        }
    }

}
