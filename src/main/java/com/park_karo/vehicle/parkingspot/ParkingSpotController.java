package com.park_karo.vehicle.parkingspot;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parkingspots")
public class ParkingSpotController {

	private static final Logger logger = LoggerFactory.getLogger(ParkingSpotController.class);
	private final ParkingSpotService parkingSpotService;

	// Track async requests for monitoring
	private final Map<String, CompletableFuture<?>> pendingRequests = new ConcurrentHashMap<>();

	public ParkingSpotController(ParkingSpotService parkingSpotService) {
		this.parkingSpotService = parkingSpotService;
	}

	// ============ SYNC ENDPOINTS (Original) ============

	@GetMapping("/test")
	public String testEndpoint() {
		logger.info("Test endpoint hit on thread: {}", Thread.currentThread().getName());
		return "Controller is working! Thread: " + Thread.currentThread().getName();
	}

	@GetMapping
	public ResponseEntity<List<ParkingSpot>> getAllParkingSpots() {
		logger.info("Sync getAllParkingSpots endpoint called");
		List<ParkingSpot> spots = parkingSpotService.findAllParkingSpots();
		return ResponseEntity.ok(spots);
	}

	@GetMapping("/nearby")
	public ResponseEntity<List<ParkingSpot>> findNearbyParkingSpots(@RequestParam double lat, @RequestParam double lon,
			@RequestParam double radiusKm) {

		logger.info("Sync findNearbyParkingSpots called: lat={}, lon={}, radius={}km", lat, lon, radiusKm);

		List<ParkingSpot> spots = parkingSpotService.findNearbyParkingSpots(lat, lon, radiusKm);
		return ResponseEntity.ok(spots);
	}

	@PostMapping
	public ResponseEntity<ParkingSpot> createParkingSpot(@RequestBody ParkingSpot parkingSpot) {
		logger.info("Sync createParkingSpot called");
		ParkingSpot savedSpot = parkingSpotService.save(parkingSpot);
		return new ResponseEntity<>(savedSpot, HttpStatus.CREATED);
	}

	// ============ ASYNC ENDPOINTS (New) ============

	/**
	 * Async: Get all parking spots
	 */
	@GetMapping("/async")
	public CompletableFuture<ResponseEntity<List<ParkingSpot>>> getAllParkingSpotsAsync() {
		String requestId = java.util.UUID.randomUUID().toString();
		logger.info("Async getAllParkingSpots started. Request ID: {}", requestId);

		CompletableFuture<ResponseEntity<List<ParkingSpot>>> future = parkingSpotService.findAllParkingSpotsAsync()
				.thenApply(spots -> {
					logger.info("Async getAllParkingSpots completed. Request ID: {}", requestId);
					return ResponseEntity.ok(spots);
				}).exceptionally(ex -> {
					logger.error("Async getAllParkingSpots failed. Request ID: {} - Error: {}", requestId,
							ex.getMessage(), ex);
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
				});

		pendingRequests.put(requestId, future);
		future.thenRun(() -> pendingRequests.remove(requestId));

		return future;
	}

	/**
	 * Async: Find nearby parking spots
	 */
	@GetMapping("/nearby/async")
	public CompletableFuture<ResponseEntity<List<ParkingSpot>>> findNearbyParkingSpotsAsync(@RequestParam double lat,
			@RequestParam double lon, @RequestParam double radiusKm) {

		String requestId = java.util.UUID.randomUUID().toString();
		logger.info("Async findNearbyParkingSpots started. Request ID: {}, lat={}, lon={}, radius={}km", requestId, lat,
				lon, radiusKm);

		CompletableFuture<ResponseEntity<List<ParkingSpot>>> future = parkingSpotService
				.findNearbyParkingSpotsAsync(lat, lon, radiusKm).thenApply(spots -> {
					logger.info("Async findNearbyParkingSpots completed. Request ID: {}, found {} spots", requestId,
							spots.size());
					return ResponseEntity.ok(spots);
				}).exceptionally(ex -> {
					logger.error("Async findNearbyParkingSpots failed. Request ID: {} - Error: {}", requestId,
							ex.getMessage(), ex);
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
				});

		pendingRequests.put(requestId, future);
		future.thenRun(() -> pendingRequests.remove(requestId));

		return future;
	}

	/**
	 * Async: Find by vehicle type
	 */
	@GetMapping("/type/async")
	public CompletableFuture<ResponseEntity<List<ParkingSpot>>> findByVehicleTypeAsync(
			@RequestParam String vehicleType) {

		String requestId = java.util.UUID.randomUUID().toString();
		logger.info("Async findByVehicleType started. Request ID: {}, type: {}", requestId, vehicleType);

		CompletableFuture<ResponseEntity<List<ParkingSpot>>> future = parkingSpotService
				.findByVehicleTypeAsync(vehicleType).thenApply(spots -> ResponseEntity.ok(spots)).exceptionally(ex -> {
					logger.error("Async findByVehicleType failed. Request ID: {} - Error: {}", requestId,
							ex.getMessage(), ex);
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
				});

		pendingRequests.put(requestId, future);
		future.thenRun(() -> pendingRequests.remove(requestId));

		return future;
	}

	/**
	 * Async: Batch create parking spots
	 */
	@PostMapping("/batch/async")
	public CompletableFuture<ResponseEntity<List<ParkingSpot>>> createParkingSpotsAsync(
			@RequestBody List<ParkingSpot> parkingSpots) {

		String requestId = java.util.UUID.randomUUID().toString();
		logger.info("Async createParkingSpots started. Request ID: {}, count: {}", requestId, parkingSpots.size());

		CompletableFuture<ResponseEntity<List<ParkingSpot>>> future = parkingSpotService.saveAllAsync(parkingSpots)
				.thenApply(savedSpots -> {
					logger.info("Async createParkingSpots completed. Request ID: {}, saved {} spots", requestId,
							savedSpots.size());
					return new ResponseEntity<>(savedSpots, HttpStatus.CREATED);
				}).exceptionally(ex -> {
					logger.error("Async createParkingSpots failed. Request ID: {} - Error: {}", requestId,
							ex.getMessage(), ex);
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
				});

		pendingRequests.put(requestId, future);
		future.thenRun(() -> pendingRequests.remove(requestId));

		return future;
	}

	/**
	 * Async: Find available spots with multiple criteria
	 */
	@GetMapping("/available/async")
	public CompletableFuture<ResponseEntity<List<ParkingSpot>>> findAvailableSpotsAsync(
			@RequestParam(required = false, defaultValue = "100.0") double maxPrice,
			@RequestParam(required = false, defaultValue = "1") int minSpaces,
			@RequestParam(required = false) String vehicleType) {

		String requestId = java.util.UUID.randomUUID().toString();
		logger.info("Async findAvailableSpots started. Request ID: {}, maxPrice={}, minSpaces={}, type={}", requestId,
				maxPrice, minSpaces, vehicleType);

		CompletableFuture<ResponseEntity<List<ParkingSpot>>> future = parkingSpotService
				.findAvailableSpotsAsync(maxPrice, minSpaces, vehicleType).thenApply(spots -> {
					logger.info("Async findAvailableSpots completed. Request ID: {}, found {} spots", requestId,
							spots.size());
					return ResponseEntity.ok(spots);
				}).exceptionally(ex -> {
					logger.error("Async findAvailableSpots failed. Request ID: {} - Error: {}", requestId,
							ex.getMessage(), ex);
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
				});

		pendingRequests.put(requestId, future);
		future.thenRun(() -> pendingRequests.remove(requestId));

		return future;
	}

	// ============ MONITORING ENDPOINTS ============

	/**
	 * Get status of pending async requests
	 */
	@GetMapping("/async/status")
	public ResponseEntity<Map<String, Object>> getAsyncStatus() {
		Map<String, Object> status = new java.util.HashMap<>();
		status.put("pendingRequests", pendingRequests.size());
		status.put("timestamp", java.time.Instant.now().toString());
		status.put("currentThread", Thread.currentThread().getName());

		List<String> requestIds = new java.util.ArrayList<>(pendingRequests.keySet());
		status.put("requestIds", requestIds);

		return ResponseEntity.ok(status);
	}

	/**
	 * Cancel a specific async request
	 */
	@DeleteMapping("/async/cancel/{requestId}")
	public ResponseEntity<Map<String, Object>> cancelAsyncRequest(@PathVariable String requestId) {
		CompletableFuture<?> future = pendingRequests.get(requestId);

		Map<String, Object> response = new java.util.HashMap<>();
		response.put("requestId", requestId);

		if (future != null && !future.isDone()) {
			boolean cancelled = future.cancel(true);
			response.put("cancelled", cancelled);
			response.put("message", cancelled ? "Request cancelled" : "Failed to cancel request");

			if (cancelled) {
				pendingRequests.remove(requestId);
			}
		} else {
			response.put("cancelled", false);
			response.put("message", "Request not found or already completed");
		}

		return ResponseEntity.ok(response);
	}

	/**
	 * Test endpoint to demonstrate async vs sync performance
	 */
	@GetMapping("/test/performance")
	public ResponseEntity<Map<String, Object>> testPerformance() {
		Map<String, Object> result = new java.util.HashMap<>();
		result.put("test", "Performance Test");
		result.put("timestamp", java.time.Instant.now().toString());
		result.put("thread", Thread.currentThread().getName());

		// Test sync operation
		long syncStart = System.currentTimeMillis();
		List<ParkingSpot> syncResult = parkingSpotService.findAllParkingSpots();
		long syncEnd = System.currentTimeMillis();
		result.put("syncTimeMs", syncEnd - syncStart);
		result.put("syncResultCount", syncResult.size());

		return ResponseEntity.ok(result);
	}

	/**
	 * Stress test endpoint (simulates heavy load)
	 */
	@GetMapping("/stress-test/async")
	public CompletableFuture<ResponseEntity<Map<String, Object>>> stressTestAsync() {
		String requestId = java.util.UUID.randomUUID().toString();
		logger.info("Stress test started. Request ID: {}", requestId);

		// Simulate multiple async operations
		List<CompletableFuture<List<ParkingSpot>>> futures = java.util.List.of(
				parkingSpotService.findAllParkingSpotsAsync(),
				parkingSpotService.findNearbyParkingSpotsAsync(19.0760, 72.8777, 5.0), // Mumbai center
				parkingSpotService.findAvailableSpotsAsync(50.0, 5, "CAR"));

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v -> {
			Map<String, Object> result = new java.util.HashMap<>();
			result.put("requestId", requestId);
			result.put("status", "All async operations completed");
			result.put("timestamp", java.time.Instant.now().toString());
			result.put("thread", Thread.currentThread().getName());

			List<Integer> counts = futures.stream().map(f -> {
				try {
					return f.get().size();
				} catch (Exception e) {
					return 0;
				}
			}).toList();

			result.put("results", counts);
			result.put("totalSpotsFound", counts.stream().mapToInt(Integer::intValue).sum());

			logger.info("Stress test completed. Request ID: {}", requestId);
			return ResponseEntity.ok(result);
		}).exceptionally(ex -> {
			logger.error("Stress test failed. Request ID: {} - Error: {}", requestId, ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		});
	}
}