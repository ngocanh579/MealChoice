package vn.codegyme.meal_choice.service.impl;


import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.codegyme.meal_choice.dto.Delivery.GeoPoint;
import vn.codegyme.meal_choice.service.DistanceService;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HeigitDistanceService
        implements DistanceService {

    private final RestTemplate restTemplate;

    @Value("${heigit.api-key}")
    private String apiKey;

    @Value("${heigit.directions-url}")
    private String directionsUrl;


    @Override
    public double calculateDistanceKm(
            GeoPoint from,
            GeoPoint to
    ) {

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        headers.set(
                "Authorization",
                apiKey
        );


        /*
         * Map API sử dụng:
         *
         * longitude trước
         * latitude sau
         */
        Map<String, Object> body =
                Map.of(
                        "coordinates",
                        List.of(

                                List.of(
                                        from.longitude(),
                                        from.latitude()
                                ),

                                List.of(
                                        to.longitude(),
                                        to.latitude()
                                )
                        )
                );


        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(
                        body,
                        headers
                );


        ResponseEntity<JsonNode> response =
                restTemplate.exchange(
                        directionsUrl,
                        HttpMethod.POST,
                        entity,
                        JsonNode.class
                );


        JsonNode root =
                response.getBody();


        if (root == null
                || root.path("routes").isEmpty()) {

            throw new RuntimeException(
                    "Không tính được khoảng cách giao hàng."
            );
        }


        double distanceMeters =
                root.path("routes")
                        .get(0)
                        .path("summary")
                        .path("distance")
                        .asDouble();


        return distanceMeters / 1000.0;
    }
}
