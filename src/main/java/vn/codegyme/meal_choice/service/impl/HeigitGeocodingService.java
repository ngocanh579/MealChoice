package vn.codegyme.meal_choice.service.impl;


import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import vn.codegyme.meal_choice.dto.Delivery.GeoPoint;
import vn.codegyme.meal_choice.service.GeocodingService;
import vn.codegyme.meal_choice.util.AddressNormalizer;
import java.net.URI;

@Service
@RequiredArgsConstructor
public class HeigitGeocodingService
        implements GeocodingService {

    private final RestTemplate restTemplate;


    @Value("${heigit.api-key}")
    private String apiKey;


    @Override
    public GeoPoint geocode(String address) {

        String normalizedAddress =
                AddressNormalizer.normalize(address);

        System.out.println("====================================");
        System.out.println("Địa chỉ gốc: " + address);
        System.out.println("Địa chỉ chuẩn hóa: " + normalizedAddress);


        // ==========================================
        // LẦN 1 - ĐỊA CHỈ ĐẦY ĐỦ
        // ==========================================

        System.out.println(
                "TRY 1: " + normalizedAddress
        );

        GeoPoint result =
                searchAddress(normalizedAddress);

        if (result != null) {

            System.out.println(
                    "SUCCESS TRY 1"
            );

            return result;
        }


        String[] parts =
                normalizedAddress.split(",");


        // ==========================================
        // LẦN 2 - BỎ PHƯỜNG
        //
        // Ba Trieu,
        // Le Dai Hanh,
        // Hai Ba Trung,
        // Ha Noi,
        // Viet Nam
        //
        // =>
        //
        // Ba Trieu, Hai Ba Trung, Ha Noi
        // ==========================================

        if (parts.length >= 4) {

            String fallback1 =
                    parts[0].trim()
                            + ", "
                            + parts[2].trim()
                            + ", "
                            + parts[3].trim();


            System.out.println(
                    "TRY 2: " + fallback1
            );


            result =
                    searchAddress(fallback1);


            if (result != null) {

                System.out.println(
                        "SUCCESS TRY 2"
                );

                return result;
            }
        }


        // ==========================================
        // LẦN 3 - CHỈ ĐƯỜNG + THÀNH PHỐ
        //
        // Ba Trieu, Ha Noi
        // ==========================================

        if (parts.length >= 4) {

            String fallback2 =
                    parts[0].trim()
                            + ", "
                            + parts[3].trim();


            System.out.println(
                    "TRY 3: " + fallback2
            );


            result =
                    searchAddress(fallback2);


            if (result != null) {

                System.out.println(
                        "SUCCESS TRY 3"
                );

                return result;
            }
        }
        throw new RuntimeException(
                "Không tìm thấy tọa độ sau khi đã thử tất cả phương án."
        );
    }
    private GeoPoint searchAddress(String searchText) {

        try {

            // ==========================================
            // 1. TẠO URL
            // ==========================================

            URI uri = UriComponentsBuilder
                    .fromUriString(
                            "https://api.openrouteservice.org/geocode/search"
                    )
                    .queryParam(
                            "text",
                            searchText
                    )
                    .queryParam(
                            "size",
                            10
                    )
                    .queryParam(
                            "boundary.country",
                            "VNM"
                    )
                    .build()
                    .encode()
                    .toUri();


            System.out.println();
            System.out.println(
                    "=============================================="
            );

            System.out.println(
                    "GEOCODING SEARCH: " + searchText
            );

            System.out.println(
                    "REQUEST URL: " + uri
            );


            // ==========================================
            // 2. HEADER
            // ==========================================

            HttpHeaders headers =
                    new HttpHeaders();

            headers.set(
                    "Authorization",
                    apiKey
            );


            HttpEntity<Void> entity =
                    new HttpEntity<>(headers);


            // ==========================================
            // 3. GỌI HEIGIT / PELIAS API
            // ==========================================

            ResponseEntity<JsonNode> response =
                    restTemplate.exchange(
                            uri,
                            HttpMethod.GET,
                            entity,
                            JsonNode.class
                    );


            System.out.println(
                    "HTTP STATUS: "
                            + response.getStatusCode()
            );


            JsonNode root =
                    response.getBody();


            if (root == null) {

                System.out.println(
                        "Response body = null"
                );

                return null;
            }


            // ==========================================
            // 4. LẤY DANH SÁCH KẾT QUẢ
            // ==========================================

            JsonNode features =
                    root.path("features");


            if (!features.isArray()
                    || features.isEmpty()) {

                System.out.println(
                        "Không có kết quả cho: "
                                + searchText
                );

                return null;
            }


            System.out.println(
                    "Số kết quả API trả về: "
                            + features.size()
            );


            // ==========================================
            // 5. DUYỆT TOÀN BỘ KẾT QUẢ
            //
            // Không lấy features.get(0) ngay
            // vì kết quả đầu tiên có thể chỉ là locality.
            // ==========================================

            for (JsonNode feature : features) {

                JsonNode properties =
                        feature.path("properties");


                String layer =
                        properties
                                .path("layer")
                                .asText("");


                String label =
                        properties
                                .path("label")
                                .asText("");


                String accuracy =
                        properties
                                .path("accuracy")
                                .asText("");


                double confidence =
                        properties
                                .path("confidence")
                                .asDouble(0);


                System.out.println();
                System.out.println(
                        "----- RESULT -----"
                );

                System.out.println(
                        "Label      : " + label
                );

                System.out.println(
                        "Layer      : " + layer
                );

                System.out.println(
                        "Accuracy   : " + accuracy
                );

                System.out.println(
                        "Confidence : " + confidence
                );


                // ==========================================
                // 6. CHỈ CHẤP NHẬN VỊ TRÍ ĐỦ CHI TIẾT
                //
                // address = địa chỉ cụ thể
                // street  = đường
                // venue   = địa điểm cụ thể
                //
                // KHÔNG nhận:
                // locality
                // region
                // county
                // localadmin
                // country
                // ...
                // ==========================================

                boolean acceptableLayer =
                        "address".equalsIgnoreCase(layer)
                                || "street".equalsIgnoreCase(layer)
                                || "venue".equalsIgnoreCase(layer);


                if (!acceptableLayer) {

                    System.out.println(
                            "REJECT: layer quá rộng -> "
                                    + layer
                    );

                    continue;
                }


                // ==========================================
                // 7. LẤY COORDINATES
                // ==========================================

                JsonNode coordinates =
                        feature
                                .path("geometry")
                                .path("coordinates");


                if (!coordinates.isArray()
                        || coordinates.size() < 2) {

                    System.out.println(
                            "REJECT: coordinates không hợp lệ."
                    );

                    continue;
                }


                /*
                 * Pelias trả:
                 *
                 * [
                 *     longitude,
                 *     latitude
                 * ]
                 */

                double longitude =
                        coordinates
                                .get(0)
                                .asDouble();


                double latitude =
                        coordinates
                                .get(1)
                                .asDouble();


                // ==========================================
                // 8. KIỂM TRA TỌA ĐỘ
                // ==========================================

                if (latitude == 0
                        || longitude == 0) {

                    System.out.println(
                            "REJECT: tọa độ bằng 0."
                    );

                    continue;
                }


                // ==========================================
                // 9. TÌM THẤY KẾT QUẢ PHÙ HỢP
                // ==========================================

                System.out.println();
                System.out.println(
                        "===== GEOCODING SUCCESS ====="
                );

                System.out.println(
                        "Search     : " + searchText
                );

                System.out.println(
                        "Label      : " + label
                );

                System.out.println(
                        "Layer      : " + layer
                );

                System.out.println(
                        "Latitude   : " + latitude
                );

                System.out.println(
                        "Longitude  : " + longitude
                );


                return new GeoPoint(
                        latitude,
                        longitude
                );
            }


            // ==========================================
            // 10. CÓ KẾT QUẢ NHƯNG KHÔNG ĐỦ CHÍNH XÁC
            // ==========================================

            System.out.println();
            System.out.println(
                    "API có kết quả nhưng không có "
                            + "address/street/venue phù hợp."
            );

            System.out.println(
                    "Search: " + searchText
            );


            return null;


        } catch (Exception e) {

            // ==========================================
            // KHÔNG THROW Ở ĐÂY
            //
            // return null để geocode()
            // tiếp tục thử fallback.
            // ==========================================

            System.out.println();
            System.out.println(
                    "===== GEOCODING ERROR ====="
            );

            System.out.println(
                    "Search: " + searchText
            );

            System.out.println(
                    "Error: " + e.getMessage()
            );

            e.printStackTrace();


            return null;
        }
    }
}
