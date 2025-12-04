package com.park_karo.vehicle.manage;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.park_karo.vehicle.MongoConnectionChecker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/manage")
public class ManageController {

	
    // 1. Declare and initialize the logger instance
    private static final Logger logger = LoggerFactory.getLogger(MongoConnectionChecker.class);
	
	@Autowired
	private ManageService manageService;

	@GetMapping("/{userId}")
	public ManageData getManageData(@PathVariable String userId) {
		// ⬅️ ADDED: This log must appear to confirm the hit.
		logger.info("LOG: *** CONTROLLER HIT for userId: " + userId + " ***");

		return manageService.getManageData(userId);
	}

	@PostMapping("/{userId}/vehicle")
	public void addVehicle(@PathVariable String userId, @RequestBody Map<String, Object> vehicle) {
		manageService.addVehicle(userId, vehicle);
	}

	@PutMapping("/{userId}/vehicle/{vehicleId}")
	public void updateVehicle(@PathVariable String userId, @PathVariable String vehicleId,
			@RequestBody Map<String, Object> vehicle) {
		manageService.updateVehicle(userId, vehicleId, vehicle);
	}

	@DeleteMapping("/{userId}/vehicle/{vehicleId}")
	public void deleteVehicle(@PathVariable String userId, @PathVariable String vehicleId) {
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
}
