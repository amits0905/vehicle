package com.park_karo.vehicle.manage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.park_karo.vehicle.dto.VehicleDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/manage")
public class ManageController {

	private static final Logger logger = LoggerFactory.getLogger(ManageController.class);

	@Autowired
	private ManageService manageService;

	@Autowired
	private Executor asyncExecutor;

	// ------------------- Sync Endpoints -------------------
	@GetMapping("/{userId}")
	public ResponseEntity<ManageData> getManageData(@PathVariable String userId) {
		logger.info("Sync getManageData called for userId: {}", userId);
		return ResponseEntity.ok(manageService.getManageData(userId));
	}

	@PostMapping("/{userId}/vehicle")
	public ResponseEntity<Void> addVehicle(@PathVariable String userId, @Valid @RequestBody VehicleDTO vehicle) {
		logger.info("Sync addVehicle called for userId: {}", userId);
		manageService.addVehicle(userId, vehicle.toMap());
		return ResponseEntity.status(201).build();
	}

	@PutMapping("/{userId}/vehicle/{vehicleId}")
	public ResponseEntity<Void> updateVehicle(@PathVariable String userId, @PathVariable String vehicleId,
			@Valid @RequestBody VehicleDTO vehicle) {
		logger.info("Sync updateVehicle called for userId: {}, vehicleId: {}", userId, vehicleId);
		manageService.updateVehicle(userId, vehicleId, vehicle.toMap());
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{userId}/vehicle/{vehicleId}")
	public ResponseEntity<Void> deleteVehicle(@PathVariable String userId, @PathVariable String vehicleId) {
		logger.info("Sync deleteVehicle called for userId: {}, vehicleId: {}", userId, vehicleId);
		manageService.deleteVehicle(userId, vehicleId);
		return ResponseEntity.noContent().build();
	}

	// ------------------- Async Endpoints -------------------
	@PostMapping("/async/batch/users")
	public CompletableFuture<ResponseEntity<Map<String, ManageData>>> getMultipleUsersAsync(
			@RequestBody List<String> userIds) {

		logger.info("Async getMultipleUsers called for {} users", userIds.size());
		return manageService.getMultipleUsersDataAsync(userIds).thenApply(ResponseEntity::ok);
	}

	@PostMapping("/async/batch/vehicles")
	public CompletableFuture<ResponseEntity<Map<String, List<String>>>> batchAddVehiclesAsync(
			@RequestBody Map<String, List<VehicleDTO>> userVehiclesMap) {

		logger.info("Async batchAddVehicles called for {} users", userVehiclesMap.size());
		return manageService.batchAddVehiclesAsync(userVehiclesMap).thenApply(ResponseEntity::ok);
	}

	@PutMapping("/async/{userId}/batch/vehicles")
	public CompletableFuture<ResponseEntity<List<String>>> batchUpdateVehiclesAsync(@PathVariable String userId,
			@RequestBody Map<String, VehicleDTO> vehicleUpdates) {

		logger.info("Async batchUpdateVehicles called for user {} with {} vehicles", userId, vehicleUpdates.size());
		return manageService.batchUpdateVehiclesAsync(userId, vehicleUpdates).thenApply(ResponseEntity::ok);
	}

	@PostMapping("/async/report")
	public CompletableFuture<ResponseEntity<Map<String, Object>>> generateReportAsync(
			@RequestBody List<String> userIds) {

		logger.info("Async generateReport called for {} users", userIds.size());
		return manageService.generateUserReportAsync(userIds).thenApply(ResponseEntity::ok);
	}

	@GetMapping("/async/test/{userId}")
	public CompletableFuture<ResponseEntity<Map<String, Object>>> asyncTest(@PathVariable String userId) {
		logger.info("Async test endpoint called for user: {}", userId);

		return CompletableFuture.supplyAsync(() -> {
			try {
				Thread.sleep(1000);
				return ResponseEntity.ok(Map.of("userId", userId, "status", "async completed", "timestamp",
						java.time.Instant.now().toString(), "thread", Thread.currentThread().getName(), "message",
						"This was processed asynchronously!"));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("Async test interrupted", e);
			}
		}, asyncExecutor);
	}
}
