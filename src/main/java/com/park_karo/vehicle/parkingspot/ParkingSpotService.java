package com.park_karo.vehicle.parkingspot;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParkingSpotService {

    private final ParkingSpotRepository parkingSpotRepository;

    public ParkingSpotService(ParkingSpotRepository parkingSpotRepository) {
        this.parkingSpotRepository = parkingSpotRepository;
    }

    // Finds all parking spots in the database (original method)
    public List<ParkingSpot> findAllParkingSpots() {
        return parkingSpotRepository.findAll();
    }

    // Saves a new parking spot (original method)
    public ParkingSpot save(ParkingSpot parkingSpot) {
        return parkingSpotRepository.save(parkingSpot);
    }

    /**
     * Finds parking spots within the specified radius of the search
     * coordinates. NOTE: This approach is inefficient for large datasets as it
     * relies on Java-side filtering. For better performance, consider
     * refactoring ParkingSpot.java to use GeoJSON Point and a 2dsphere index in
     * MongoDB.
     */
    public List<ParkingSpot> findNearbyParkingSpots(double searchLat, double searchLon, double radiusKm) {

        // 1. Fetch all spots (inefficient for many spots)
        List<ParkingSpot> allSpots = parkingSpotRepository.findAll(); // Fetches all data

        // 2. Filter the spots in Java using the Haversine distance formula
        return allSpots.stream()
                .filter(spot -> {
                    double distance = calculateDistance(searchLat, searchLon, spot.getLatitude(), spot.getLongitude()); // Uses existing lat/lon fields
                    return distance <= radiusKm;
                })
                .toList();
    }

    /**
     * Calculates the distance between two latitude and longitude points using
     * the Haversine formula. Distance is returned in Kilometers (km).
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Final distance in km
    }
}
