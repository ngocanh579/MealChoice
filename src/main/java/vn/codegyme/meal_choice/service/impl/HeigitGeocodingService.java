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
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class HeigitGeocodingService implements GeocodingService {

    private final RestTemplate restTemplate;

    @Value("${heigit.api-key}")
    private String apiKey;

    // ================================================================
    //  PUBLIC API
    // ================================================================

    /**
     * Chuyển địa chỉ văn bản → tọa độ (lat, lng).
     *
     * Địa chỉ từ hệ thống có format:
     *   street, ward, district, city[, Việt Nam]
     * Sau AddressNormalizer sẽ thành 4–5 phần không dấu.
     *
     * Thử lần lượt 4 cấp độ rút gọn, từ nghiêm → lỏng.
     */
    @Override
    public GeoPoint geocode(String address) {

        String normalized = AddressNormalizer.normalize(address);

        System.out.println("====================================");
        System.out.println("Địa chỉ gốc      : " + address);
        System.out.println("Địa chỉ chuẩn hóa: " + normalized);

        // Tách thành các phần và trim
        String[] parts = Arrays.stream(normalized.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        int n = parts.length;

        // ── LẦN 1 ─ Địa chỉ đầy đủ (strict layer) ─────────────────
        System.out.println("TRY 1: " + normalized);
        GeoPoint r = searchAddress(normalized, true);
        if (r != null) { System.out.println("SUCCESS TRY 1"); return r; }

        // ── LẦN 2 ─ Bỏ ward, giữ street + district + city + country ─
        // parts = [street, ward, district, city, "Viet Nam"]  (n=5)
        //       => street, district, city, Viet Nam
        if (n >= 4) {
            // Dùng chỉ số từ cuối để đúng với mọi độ dài
            String try2 = parts[0]
                    + ", " + parts[n - 3]   // district
                    + ", " + parts[n - 2]   // city
                    + ", " + parts[n - 1];  // "Viet Nam"
            System.out.println("TRY 2: " + try2);
            r = searchAddress(try2, true);
            if (r != null) { System.out.println("SUCCESS TRY 2"); return r; }
        }

        // ── LẦN 3 ─ district + city + country (loose layer) ─────────
        if (n >= 3) {
            String try3 = parts[n - 3]          // district
                    + ", " + parts[n - 2]        // city
                    + ", " + parts[n - 1];       // "Viet Nam"
            System.out.println("TRY 3: " + try3);
            r = searchAddress(try3, false);   // chấp nhận neighbourhood/locality
            if (r != null) { System.out.println("SUCCESS TRY 3"); return r; }
        }

        // ── LẦN 4 ─ city + country (fallback tối thiểu, loose layer) ─
        if (n >= 2) {
            String try4 = parts[n - 2] + ", " + parts[n - 1]; // city + Viet Nam
            System.out.println("TRY 4: " + try4);
            r = searchAddress(try4, false);
            if (r != null) { System.out.println("SUCCESS TRY 4 (city approximation)"); return r; }
        }

        throw new RuntimeException(
                "Không tìm thấy tọa độ cho địa chỉ: \"" + address + "\". "
                + "Vui lòng kiểm tra lại địa chỉ giao hàng."
        );
    }

    // ================================================================
    //  PRIVATE HELPERS
    // ================================================================

    /**
     * Gọi Pelias (ORS) geocode API.
     *
     * @param strictLayer true  → chỉ chấp nhận layer: address / street / venue
     *                    false → chấp nhận thêm: neighbourhood, locality, county,
     *                            localadmin (nhưng vẫn loại country / region)
     */
    private GeoPoint searchAddress(String searchText, boolean strictLayer) {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://api.openrouteservice.org/geocode/search")
                    .queryParam("text",             searchText)
                    .queryParam("size",             10)
                    .queryParam("boundary.country", "VNM")
                    .build()
                    .encode()
                    .toUri();

            System.out.println();
            System.out.println("==============================================");
            System.out.println("GEOCODING SEARCH : " + searchText);
            System.out.println("STRICT_LAYER     : " + strictLayer);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", apiKey);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    uri, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);

            System.out.println("HTTP STATUS: " + response.getStatusCode());

            JsonNode root = response.getBody();
            if (root == null) { System.out.println("Response body = null"); return null; }

            JsonNode features = root.path("features");
            if (!features.isArray() || features.isEmpty()) {
                System.out.println("Không có kết quả cho: " + searchText);
                return null;
            }

            System.out.println("Số kết quả API: " + features.size());

            for (JsonNode feature : features) {
                JsonNode props = feature.path("properties");

                String layer      = props.path("layer").asText("");
                String label      = props.path("label").asText("");
                double confidence = props.path("confidence").asDouble(0);

                System.out.println("----- RESULT -----");
                System.out.println("Layer      : " + layer);
                System.out.println("Label      : " + label);
                System.out.println("Confidence : " + confidence);

                // ── Kiểm tra layer ─────────────────────────────────
                boolean acceptable;
                if (strictLayer) {
                    // Chỉ chấp nhận địa chỉ cụ thể
                    acceptable = "address".equalsIgnoreCase(layer)
                            || "street".equalsIgnoreCase(layer)
                            || "venue".equalsIgnoreCase(layer);
                } else {
                    // Fallback: loại bỏ country và region, chấp nhận phần còn lại
                    acceptable = !"country".equalsIgnoreCase(layer)
                            && !"region".equalsIgnoreCase(layer);
                }

                if (!acceptable) {
                    System.out.println("REJECT layer: " + layer);
                    continue;
                }

                // ── Lấy tọa độ ─────────────────────────────────────
                JsonNode coords = feature.path("geometry").path("coordinates");
                if (!coords.isArray() || coords.size() < 2) {
                    System.out.println("REJECT: coordinates không hợp lệ");
                    continue;
                }

                // Pelias trả [longitude, latitude]
                double lng = coords.get(0).asDouble();
                double lat = coords.get(1).asDouble();

                if (lat == 0 || lng == 0) {
                    System.out.println("REJECT: tọa độ = 0");
                    continue;
                }

                System.out.println("===== GEOCODING SUCCESS =====");
                System.out.println("Latitude  : " + lat);
                System.out.println("Longitude : " + lng);

                return new GeoPoint(lat, lng);
            }

            System.out.println("API có kết quả nhưng không có layer phù hợp. Search: " + searchText);
            return null;

        } catch (Exception e) {
            System.out.println("===== GEOCODING ERROR =====");
            System.out.println("Search: " + searchText);
            System.out.println("Error : " + e.getMessage());
            e.printStackTrace();
            return null;    // trả null để geocode() tiếp tục fallback
        }
    }
}
