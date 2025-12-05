package com.park_karo.vehicle.parkingspot;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ParkingSpotService {

	private static final Logger logger = LoggerFactory.getLogger(ParkingSpotService.class);
	private final ParkingSpotRepository parkingSpotRepository;

	public ParkingSpotService(ParkingSpotRepository parkingSpotRepository) {
		this.parkingSpotRepository = parkingSpotRepository;
	}

	// ============ SYNC METHODS (Original) ============

	public List<ParkingSpot> findAllParkingSpots() {
		return parkingSpotRepository.findAll();
	}

	public ParkingSpot save(ParkingSpot parkingSpot) {
		return parkingSpotRepository.save(parkingSpot);
	}

	public List<ParkingSpot> findNearbyParkingSpots(double searchLat, double searchLon, double radiusKm) {
		logger.info("Finding nearby spots synchronously for lat: {}, lon: {}, radius: {}km", searchLat, searchLon,
				radiusKm);

		List<ParkingSpot> allSpots = parkingSpotRepository.findAll();

		return allSpots.stream().filter(spot -> {
			double distance = calculateDistance(searchLat, searchLon, spot.getLatitude(), spot.getLongitude());
			return distance <= radiusKm;
		}).toList();
	}

	// ============ ASYNC METHODS (New) ============

	/**
	 * Async: Find all parking spots
	 */
	@Async("threadPoolTaskExecutor")
	public CompletableFuture<List<ParkingSpot>> findAllParkingSpotsAsync() {
		logger.info("Async findAllParkingSpots started on thread: {}", Thread.currentThread().getName());

		try {
			List<ParkingSpot> spots = parkingSpotRepository.findAll();
			logger.info("Async findAllParkingSpots completed. Found {} spots", spots.size());
			return CompletableFuture.completedFuture(spots);
		} catch (Exception e) {
			logger.error("Async findAllParkingSpots failed: {}", e.getMessage(), e);
			return CompletableFuture.failedFuture(e);
		}
	}

	/**
	 * Async: Find nearby parking spots with parallel processing
	 */
	@Async("threadPoolTaskExecutor")
	public CompletableFuture<List<ParkingSpot>> findNearbyParkingSpotsAsync(double searchLat, double searchLon,
			double radiusKm) {

		logger.info("Async findNearbyParkingSpots started on thread: {} for lat: {}, lon: {}, radius: {}km",
				Thread.currentThread().getName(), searchLat, searchLon, radiusKm);

		try {
			List<ParkingSpot> allSpots = parkingSpotRepository.findAll();

			// Use parallel stream for faster filtering
			List<ParkingSpot> nearbySpots = allSpots.parallelStream().filter(spot -> {
				double distance = calculateDistance(searchLat, searchLon, spot.getLatitude(), spot.getLongitude());
				return distance <= radiusKm;
			}).toList();

			logger.info("Async findNearbyParkingSpots completed. Found {} nearby spots out of {} total",
					nearbySpots.size(), allSpots.size());

			return CompletableFuture.completedFuture(nearbySpots);
		} catch (Exception e) {
			logger.error("Async findNearbyParkingSpots failed: {}", e.getMessage(), e);
			return CompletableFuture.failedFuture(e);
		}
	}

	/**
	 * Async: Find by vehicle type
	 */
	@Async("threadPoolTaskExecutor")
	public CompletableFuture<List<ParkingSpot>> findByVehicleTypeAsync(String vehicleType) {
		logger.info("Async findByVehicleType started on thread: {} for type: {}", Thread.currentThread().getName(),
				vehicleType);

		try {
			// Simulate database query
			Thread.sleep(100); // Simulate DB delay

			List<ParkingSpot> allSpots = parkingSpotRepository.findAll();
			List<ParkingSpot> filteredSpots = allSpots.stream()
					.filter(spot -> vehicleType.equalsIgnoreCase(spot.getVehicleType())).toList();

			return CompletableFuture.completedFuture(filteredSpots);
		} catch (Exception e) {
			logger.error("Async findByVehicleType failed: {}", e.getMessage(), e);
			return CompletableFuture.failedFuture(e);
		}
	}

	/**
	 * Async: Batch save multiple parking spots
	 */
	@Async("threadPoolTaskExecutor")
	public CompletableFuture<List<ParkingSpot>> saveAllAsync(List<ParkingSpot> parkingSpots) {
		logger.info("Async saveAll started on thread: {} for {} spots", Thread.currentThread().getName(),
				parkingSpots.size());

		try {
			List<ParkingSpot> savedSpots = parkingSpotRepository.saveAll(parkingSpots);
			logger.info("Async saveAll completed. Saved {} spots", savedSpots.size());
			return CompletableFuture.completedFuture(savedSpots);
		} catch (Exception e) {
			logger.error("Async saveAll failed: {}", e.getMessage(), e);
			return CompletableFuture.failedFuture(e);
		}
	}

	/**
	 * Async: Complex operation - Find available spots by multiple criteria
	 */
	@Async("threadPoolTaskExecutor")
	public CompletableFuture<List<ParkingSpot>> findAvailableSpotsAsync(double maxPrice, int minSpaces,
			String vehicleType) {

		logger.info(
				"Async findAvailableSpots started on thread: {} with criteria - maxPrice: {}, minSpaces: {}, type: {}",
				Thread.currentThread().getName(), maxPrice, minSpaces, vehicleType);

		try {
			List<ParkingSpot> allSpots = parkingSpotRepository.findAll();

			// Process in parallel
			List<ParkingSpot> availableSpots = allSpots.parallelStream()
					.filter(spot -> spot.getHourlyRate() <= maxPrice)
					.filter(spot -> spot.getAvailableSpaces() >= minSpaces).filter(spot -> vehicleType == null
							|| vehicleType.isEmpty() || vehicleType.equalsIgnoreCase(spot.getVehicleType()))
					.toList();

			logger.info("Async findAvailableSpots completed. Found {} available spots", availableSpots.size());

			return CompletableFuture.completedFuture(availableSpots);
		} catch (Exception e) {
			logger.error("Async findAvailableSpots failed: {}", e.getMessage(), e);
			return CompletableFuture.failedFuture(e);
		}
	}

	// ============ HELPER METHOD ============

	/**
	 * Calculates the distance between two latitude and longitude points using the
	 * Haversine formula. Distance is returned in Kilometers (km).
	 */
	private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
		final int R = 6371; // Radius of the earth in km
		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);

		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return R * c; // Final distance in km
	}
}