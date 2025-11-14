package uth.edu.vn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uth.edu.vn.entity.TramSac;
import uth.edu.vn.entity.Charger;
import uth.edu.vn.entity.DatCho;
import uth.edu.vn.entity.PhienSac;
import uth.edu.vn.entity.User;
import uth.edu.vn.service.EVDriverService;
import uth.edu.vn.repository.TramSacRepository;
import uth.edu.vn.repository.ChargerRepository;
import uth.edu.vn.enums.PointStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stations Controller
 * REST API endpoints cho quản lý trạm sạc và điểm sạc
 */
@RestController
@RequestMapping("/api/stations")
public class StationsController {
    
    @Autowired
    private EVDriverService evDriverService;
    
    @Autowired
    private TramSacRepository tramSacRepository;
    
    @Autowired
    private ChargerRepository chargerRepository;
    
    /**
     * Lấy danh sách tất cả trạm sạc
     * GET /api/stations
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllStations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        try {
            List<TramSac> stations;
            
            if (search != null && !search.trim().isEmpty()) {
                stations = tramSacRepository.searchStations(search.trim());
            } else {
                stations = tramSacRepository.findAll();
            }
            
            // Convert to response format
            List<Map<String, Object>> stationList = new ArrayList<>();
            
            for (TramSac station : stations) {
                Map<String, Object> stationData = new HashMap<>();
                stationData.put("id", station.getId());
                stationData.put("name", station.getName());
                stationData.put("address", station.getAddress());
                stationData.put("latitude", station.getLatitude());
                stationData.put("longitude", station.getLongitude());
                stationData.put("status", station.getStatus());
                stationData.put("operatingHours", "24/7"); // Default value
                stationData.put("contactInfo", "Liên hệ hotline"); // Default value
                
                // Đếm số điểm sạc khả dụng
                Long availableChargers = chargerRepository.countAvailableChargersByStation(station.getId());
                List<Charger> allChargers = chargerRepository.findByChargingStationId(station.getId());
                
                stationData.put("totalChargers", allChargers.size());
                stationData.put("availableChargers", availableChargers != null ? availableChargers : 0);
                
                // Thông tin các loại connector
                Map<String, Integer> connectorTypes = new HashMap<>();
                for (Charger charger : allChargers) {
                    String type = charger.getConnectorType().toString();
                    connectorTypes.put(type, connectorTypes.getOrDefault(type, 0) + 1);
                }
                stationData.put("connectorTypes", connectorTypes);
                
                stationList.add(stationData);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("stations", stationList);
            response.put("total", stationList.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi lấy danh sách trạm sạc: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Tìm trạm sạc gần đây
     * GET /api/stations/nearby
     */
    @GetMapping("/nearby")
    public ResponseEntity<Map<String, Object>> getNearbyStations(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radius) {
        try {
            List<TramSac> nearbyStations = evDriverService.findNearbyStations(latitude, longitude, radius);
            
            if (nearbyStations == null) {
                nearbyStations = new ArrayList<>();
            }
            
            // Convert to response format với thông tin distance
            List<Map<String, Object>> stationList = new ArrayList<>();
            
            for (TramSac station : nearbyStations) {
                Map<String, Object> stationData = new HashMap<>();
                stationData.put("id", station.getId());
                stationData.put("name", station.getName());
                stationData.put("address", station.getAddress());
                stationData.put("latitude", station.getLatitude());
                stationData.put("longitude", station.getLongitude());
                stationData.put("status", station.getStatus());
                
                // Tính khoảng cách (simplified calculation)
                double distance = calculateDistance(latitude, longitude, 
                    station.getLatitude(), station.getLongitude());
                stationData.put("distance", Math.round(distance * 100.0) / 100.0);
                
                // Đếm số điểm sạc khả dụng
                Long availableChargers = chargerRepository.countAvailableChargersByStation(station.getId());
                List<Charger> allChargers = chargerRepository.findByChargingStationId(station.getId());
                
                stationData.put("totalChargers", allChargers.size());
                stationData.put("availableChargers", availableChargers != null ? availableChargers : 0);
                
                stationList.add(stationData);
            }
            
            // Sắp xếp theo khoảng cách
            stationList.sort((a, b) -> Double.compare((Double) a.get("distance"), (Double) b.get("distance")));
            
            Map<String, Object> response = new HashMap<>();
            response.put("stations", stationList);
            response.put("total", stationList.size());
            response.put("searchCenter", Map.of("latitude", latitude, "longitude", longitude));
            response.put("radius", radius);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi tìm trạm sạc gần đây: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Lấy chi tiết trạm sạc theo ID
     * GET /api/stations/{stationId}
     */
    @GetMapping("/{stationId}")
    public ResponseEntity<Map<String, Object>> getStationDetails(@PathVariable Long stationId) {
        try {
            TramSac station = tramSacRepository.findById(stationId).orElse(null);
            
            if (station == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Không tìm thấy trạm sạc");
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> stationData = new HashMap<>();
            stationData.put("id", station.getId());
            stationData.put("name", station.getName());
            stationData.put("address", station.getAddress());
            stationData.put("latitude", station.getLatitude());
            stationData.put("longitude", station.getLongitude());
            stationData.put("status", station.getStatus());
            stationData.put("operatingHours", "24/7"); // Default value
            stationData.put("contactInfo", "Liên hệ hotline"); // Default valueault value
            
            // Lấy danh sách các điểm sạc
            List<Charger> chargers = chargerRepository.findByChargingStationId(stationId);
            List<Map<String, Object>> chargerList = new ArrayList<>();
            
            for (Charger charger : chargers) {
                Map<String, Object> chargerData = new HashMap<>();
                chargerData.put("id", charger.getPointId());
                chargerData.put("name", charger.getPointName());
                chargerData.put("connectorType", charger.getConnectorType());
                chargerData.put("powerOutput", charger.getPowerCapacity());
                chargerData.put("status", charger.getStatus());
                chargerData.put("pricePerKwh", charger.getPricePerKwh());
                chargerList.add(chargerData);
            }
            
            stationData.put("chargers", chargerList);
            stationData.put("totalChargers", chargers.size());
            
            Long availableCount = chargers.stream()
                .mapToLong(c -> c.getStatus() == PointStatus.AVAILABLE ? 1 : 0)
                .sum();
            stationData.put("availableChargers", availableCount);
            
            return ResponseEntity.ok(stationData);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi lấy chi tiết trạm sạc: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Lấy các điểm sạc khả dụng tại trạm
     * GET /api/stations/{stationId}/available-chargers
     */
    @GetMapping("/{stationId}/available-chargers")
    public ResponseEntity<Map<String, Object>> getAvailableChargers(@PathVariable Long stationId) {
        try {
            List<Charger> availableChargers = evDriverService.getAvailablePoints(stationId);
            
            if (availableChargers == null) {
                availableChargers = new ArrayList<>();
            }
            
            List<Map<String, Object>> chargerList = new ArrayList<>();
            
            for (Charger charger : availableChargers) {
                Map<String, Object> chargerData = new HashMap<>();
                chargerData.put("id", charger.getPointId());
                chargerData.put("name", charger.getPointName());
                chargerData.put("connectorType", charger.getConnectorType());
                chargerData.put("powerOutput", charger.getPowerCapacity());
                chargerData.put("status", charger.getStatus());
                chargerData.put("pricePerKwh", charger.getPricePerKwh());
                chargerList.add(chargerData);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("chargers", chargerList);
            response.put("stationId", stationId);
            response.put("total", chargerList.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi lấy điểm sạc khả dụng: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Đặt chỗ điểm sạc
     * POST /api/stations/{stationId}/chargers/{chargerId}/book
     */
    @PostMapping("/{stationId}/chargers/{chargerId}/book")
    public ResponseEntity<Map<String, Object>> bookCharger(
            @PathVariable Long stationId,
            @PathVariable Long chargerId,
            @RequestBody Map<String, Object> bookingRequest,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = evDriverService.findUserByEmail(email);
            
            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Không tìm thấy người dùng");
                return ResponseEntity.notFound().build();
            }
            
            // Parse thời gian từ request
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime startTime = LocalDateTime.parse((String) bookingRequest.get("startTime"), formatter);
            LocalDateTime endTime = LocalDateTime.parse((String) bookingRequest.get("endTime"), formatter);
            
            DatCho booking = evDriverService.createBooking(user.getId(), chargerId, startTime, endTime);
            
            Map<String, Object> response = new HashMap<>();
            if (booking != null) {
                response.put("success", true);
                response.put("bookingId", booking.getBookingId());
                response.put("message", "Đặt chỗ thành công");
                response.put("startTime", booking.getStartTime().format(formatter));
                response.put("endTime", booking.getEndTime().format(formatter));
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Không thể đặt chỗ. Điểm sạc có thể đã được đặt.");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi đặt chỗ: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Bắt đầu phiên sạc
     * POST /api/stations/{stationId}/chargers/{chargerId}/start-charging
     */
    @PostMapping("/{stationId}/chargers/{chargerId}/start-charging")
    public ResponseEntity<Map<String, Object>> startCharging(
            @PathVariable Long stationId,
            @PathVariable Long chargerId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = evDriverService.findUserByEmail(email);
            
            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Không tìm thấy người dùng");
                return ResponseEntity.notFound().build();
            }
            
            PhienSac session = evDriverService.startChargingSession(user.getId(), chargerId);
            
            Map<String, Object> response = new HashMap<>();
            if (session != null) {
                response.put("success", true);
                response.put("sessionId", session.getSessionId());
                response.put("qrCode", session.getQrCode());
                response.put("message", "Bắt đầu sạc thành công");
                response.put("startTime", session.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Không thể bắt đầu sạc. Điểm sạc có thể không khả dụng.");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi bắt đầu sạc: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Tính khoảng cách giữa 2 điểm (Haversine formula - simplified)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // convert to kilometers
        
        return distance;
    }
}