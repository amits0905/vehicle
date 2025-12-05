package com.park_karo.vehicle.manage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manage")
public class ManageController {

	private static final Logger logger = LoggerFactory.getLogger(ManageController.class);

	@Autowired
	private ManageService manageService;

	// ============ SYNC ENDPOINTS (Original) ============

	@GetMapping("/{userId}")
	public ManageData getManageData(@PathVariable String userId) {
		logger.info("Sync getManageData called for userId: {}", userId);
		return manageService.getManageData(userId);
	}

	@PostMapping("/{userId}/vehicle")
	public void addVehicle(@PathVariable String userId, @RequestBody Map<String, Object> vehicle) {
		logger.info("Sync addVehicle called for userId: {}", userId);
		manageService.addVehicle(userId, vehicle);
	}

	@PutMapping("/{userId}/vehicle/{vehicleId}")
	public void updateVehicle(@PathVariable String userId, @PathVariable String vehicleId,
			@RequestBody Map<String, Object> vehicle) {
		logger.info("Sync updateVehicle called for userId: {}, vehicleId: {}", userId, vehicleId);
		manageService.updateVehicle(userId, vehicleId, vehicle);
	}

	@DeleteMapping("/{userId}/vehicle/{vehicleId}")
	public void deleteVehicle(@PathVariable String userId, @PathVariable String vehicleId) {
		logger.info("Sync deleteVehicle called for userId: {}, vehicleId: {}", userId, vehicleId);
		manageService.deleteVehicle(userId, vehicleId);
	}

	@PostMapping("/{userId}/favoriteSpot")
	public void addFavoriteSpot(@PathVariable String userId, @RequestBody Map<String, Object> spot) {
		manageService.addFavoriteSpot(userId, spot);
	}

	@PutMapping("/{userId}/favoriteSpot/{spotId}")
	public void updateFavoriteSpot(@PathVariable String userId, @PathVariable String spotId,
			@RequestBody Map<String, Object> spot) {
		manageService.updateFavoriteSpot(userId, spotId, spot);
	}

	@DeleteMapping("/{userId}/favoriteSpot/{spotId}")
	public void deleteFavoriteSpot(@PathVariable String userId, @PathVariable String spotId) {
		manageService.deleteFavoriteSpot(userId, spotId);
	}

	@PostMapping("/{userId}/history")
	public void addHistory(@PathVariable String userId, @RequestBody Map<String, Object> historyItem) {
		manageService.addHistory(userId, historyItem);
	}

	@PutMapping("/{userId}/history/{historyId}")
	public void updateHistory(@PathVariable String userId, @PathVariable String historyId,
			@RequestBody Map<String, Object> historyItem) {
		manageService.updateHistory(userId, historyId, historyItem);
	}

	@DeleteMapping("/{userId}/history/{historyId}")
	public void deleteHistory(@PathVariable String userId, @PathVariable String historyId) {
		manageService.deleteHistory(userId, historyId);
	}

	@PostMapping("/{userId}/activeStatus")
	public void addActiveStatus(@PathVariable String userId, @RequestBody Map<String, Object> status) {
		manageService.addActiveStatus(userId, status);
	}

	@PutMapping("/{userId}/activeStatus/{statusId}")
	public void updateActiveStatus(@PathVariable String userId, @PathVariable String statusId,
			@RequestBody Map<String, Object> status) {
		manageService.updateActiveStatus(userId, statusId, status);
	}

	@DeleteMapping("/{userId}/activeStatus/{statusId}")
	public void deleteActiveStatus(@PathVariable String userId, @PathVariable String statusId) {
		manageService.deleteActiveStatus(userId, statusId);
	}

	// ============ ASYNC ENDPOINTS (New) ============

	/**
	 * Async: Get data for multiple users
	 */
	@PostMapping("/async/batch/users")
	public CompletableFuture<ResponseEntity<Map<String, ManageData>>> getMultipleUsersAsync(
			@RequestBody List<String> userIds) {

		logger.info("Async getMultipleUsers called for {} users", userIds.size());

		return manageService.getMultipleUsersDataAsync(userIds).thenApply(ResponseEntity::ok).exceptionally(ex -> {
			logger.error("Async getMultipleUsers failed: {}", ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		});
	}

	/**
	 * Async: Batch add vehicles for multiple users
	 */
	@PostMapping("/async/batch/vehicles")
	public CompletableFuture<ResponseEntity<Map<String, List<String>>>> batchAddVehiclesAsync(
			@RequestBody Map<String, List<Map<String, Object>>> userVehiclesMap) {

		logger.info("Async batchAddVehicles called for {} users", userVehiclesMap.size());

		return manageService.batchAddVehiclesAsync(userVehiclesMap).thenApply(ResponseEntity::ok).exceptionally(ex -> {
			logger.error("Async batchAddVehicles failed: {}", ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		});
	}

	/**
	 * Async: Batch update vehicles for a user
	 */
	@PutMapping("/async/{userId}/batch/vehicles")
	public CompletableFuture<ResponseEntity<List<String>>> batchUpdateVehiclesAsync(@PathVariable String userId,
			@RequestBody Map<String, Map<String, Object>> vehicleUpdates) {

		logger.info("Async batchUpdateVehicles called for user {} with {} vehicles", userId, vehicleUpdates.size());

		return manageService.batchUpdateVehiclesAsync(userId, vehicleUpdates).thenApply(ResponseEntity::ok)
				.exceptionally(ex -> {
					logger.error("Async batchUpdateVehicles failed: {}", ex.getMessage(), ex);
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
				});
	}

	/**
	 * Async: Generate user report
	 */
	@PostMapping("/async/report")
	public CompletableFuture<ResponseEntity<Map<String, Object>>> generateReportAsync(
			@RequestBody List<String> userIds) {

		logger.info("Async generateReport called for {} users", userIds.size());

		return manageService.generateUserReportAsync(userIds).thenApply(ResponseEntity::ok).exceptionally(ex -> {
			logger.error("Async generateReport failed: {}", ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		});
	}

	/**
	 * Async performance test
	 */
	@GetMapping("/async/test/{userId}")
	public CompletableFuture<ResponseEntity<Map<String, Object>>> asyncTest(@PathVariable String userId) {
		logger.info("Async test endpoint called for user: {}", userId);

		return CompletableFuture.supplyAsync(() -> {
			try {
				// Simulate some async work
				Thread.sleep(1000);

				Map<String, Object> result = Map.of("userId", userId, "status", "async completed", "timestamp",
						java.time.Instant.now().toString(), "thread", Thread.currentThread().getName(), "message",
						"This was processed asynchronously!");

				return ResponseEntity.ok(result);

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
		});
	}
}