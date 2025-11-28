package com.park_karo.vehicle.parkingspot.controller;

import com.park_karo.vehicle.parkingspot.model.ParkingSpot;
import com.park_karo.vehicle.parkingspot.service.ParkingSpotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parkingspots") // Base path for all methods in this controller
public class ParkingSpotController {

    private final ParkingSpotService parkingSpotService;

    public ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }

    // Original GET: http://localhost:8080/api/v1/parkingspots
    @GetMapping
    public ResponseEntity<List<ParkingSpot>> getAllParkingSpots() {
        List<ParkingSpot> spots = parkingSpotService.findAllParkingSpots();
        return ResponseEntity.ok(spots);
    }

    /**
     * NEW GET ENDPOINT: Find nearby spots. Example:
     * http://localhost:8080/api/v1/parkingspots/nearby?lat=19.0760&lon=72.8777&radiusKm=5
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<ParkingSpot>> findNearbyParkingSpots(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam double radiusKm) {

        List<ParkingSpot> spots = parkingSpotService.findNearbyParkingSpots(lat, lon, radiusKm);
        return ResponseEntity.ok(spots);
    }

    // Original POST: http://localhost:8080/api/v1/parkingspots
    @PostMapping
    public ResponseEntity<ParkingSpot> createParkingSpot(@RequestBody ParkingSpot parkingSpot) {
        ParkingSpot savedSpot = parkingSpotService.save(parkingSpot);
        return new ResponseEntity<>(savedSpot, HttpStatus.CREATED);
    }
}
