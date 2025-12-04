package com.park_karo.vehicle.manage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ManageService {

	private static final Logger logger = LoggerFactory.getLogger(ManageService.class);

	@Autowired
	private ManageDAO manageDAO;

	private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

	// ============ GET OPERATIONS ============

	public ManageData getManageData(String userId) {
		try {
			logger.info("Service: Getting manage data for userId: {}", userId);

			// Validate input
			if (userId == null || userId.trim().isEmpty()) {
				logger.error("Service: Invalid userId provided: {}", userId);
				throw new IllegalArgumentException("User ID cannot be null or empty");
			}

			Map<String, Object> dbData = manageDAO.findUserData(userId);

			if (dbData == null) {
				logger.info("Service: No data found, creating new for userId: {}", userId);
				// Create initial structure
				Map<String, Object> newUserData = createInitialUserData(userId);

				try {
					manageDAO.saveUserData(userId, newUserData);
				} catch (Exception e) {
					logger.error("Service: Failed to save new user data for {}: {}", userId, e.getMessage());
					throw new RuntimeException("Failed to initialize user data", e);
				}

				return convertToManageData(newUserData);
			}

			logger.info("Service: Found existing data for userId: {}", userId);
			return convertToManageData(dbData);

		} catch (IllegalArgumentException e) {
			// Re-throw validation errors
			throw e;
		} catch (RuntimeException e) {
			// Re-throw runtime exceptions
			throw e;
		} catch (Exception e) {
			// Catch any unexpected exceptions
			logger.error("Service: Unexpected error getting manage data for {}: {}", userId, e.getMessage(), e);
			throw new RuntimeException("Failed to retrieve user data", e);
		}
	}

	// ============ VEHICLE OPERATIONS ============

	public void addVehicle(String userId, Map<String, Object> vehicle) {
		try {
			logger.info("Service: Adding vehicle for userId: {}", userId);

			// Validate inputs
			validateUserId(userId);
			validateVehicleData(vehicle);

			Map<String, Object> userData = manageDAO.findUserData(userId);

			if (userData == null) {
				userData = createInitialUserData(userId);
			}

			List<Map<String, Object>> vehicles = getOrCreateList(userData, "vehicles");

			// Add timestamps
			String now = LocalDateTime.now().format(formatter);
			vehicle.put("created_at", now);
			vehicle.put("updated_at", now);

			// Check for duplicate vehicle_id
			String vehicleId = (String) vehicle.get("vehicle_id");
			if (vehicleId == null || vehicleId.trim().isEmpty()) {
				throw new IllegalArgumentException("Vehicle ID is required");
			}

			// Remove existing vehicle with same ID (update scenario)
			vehicles.removeIf(v -> vehicleId.equals(v.get("vehicle_id")));

			// Add to list
			vehicles.add(vehicle);

			// Update in database
			manageDAO.updateField(userId, "vehicles", vehicles);
			manageDAO.updateField(userId, "updated_at", now);

			logger.info("Service: Successfully added vehicle {} for userId: {}", vehicleId, userId);

		} catch (IllegalArgumentException e) {
			logger.error("Service: Validation error adding vehicle for {}: {}", userId, e.getMessage());
			throw e;
		} catch (RuntimeException e) {
			logger.error("Service: Error adding vehicle for {}: {}", userId, e.getMessage(), e);
			throw new RuntimeException("Failed to add vehicle: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Service: Unexpected error adding vehicle for {}: {}", userId, e.getMessage(), e);
			throw new RuntimeException("An unexpected error occurred while adding vehicle", e);
		}
	}

	public void updateVehicle(String userId, String vehicleId, Map<String, Object> updatedVehicle) {
		try {
			logger.info("Service: Updating vehicle {} for userId: {}", vehicleId, userId);

			// Validate inputs
			validateUserId(userId);
			validateVehicleId(vehicleId);
			validateVehicleData(updatedVehicle);

			Map<String, Object> userData = manageDAO.findUserData(userId);
			if (userData == null) {
				logger.warn("Service: User not found for userId: {}", userId);
				throw new IllegalArgumentException("User not found: " + userId);
			}

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> vehicles = (List<Map<String, Object>>) userData.get("vehicles");
			if (vehicles == null) {
				logger.warn("Service: No vehicles found for userId: {}", userId);
				throw new IllegalArgumentException("No vehicles found for user: " + userId);
			}

			boolean found = false;
			for (Map<String, Object> vehicle : vehicles) {
				if (vehicleId.equals(vehicle.get("vehicle_id"))) {
					// Update fields
					vehicle.putAll(updatedVehicle);
					vehicle.put("updated_at", LocalDateTime.now().format(formatter));
					found = true;
					break;
				}
			}

			if (found) {
				String now = LocalDateTime.now().format(formatter);
				manageDAO.updateField(userId, "vehicles", vehicles);
				manageDAO.updateField(userId, "updated_at", now);
				logger.info("Service: Vehicle {} updated for userId: {}", vehicleId, userId);
			} else {
				logger.warn("Service: Vehicle {} not found for userId: {}", vehicleId, userId);
				throw new IllegalArgumentException("Vehicle not found: " + vehicleId);
			}

		} catch (IllegalArgumentException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.error("Service: Error updating vehicle {} for {}: {}", vehicleId, userId, e.getMessage(), e);
			throw new RuntimeException("Failed to update vehicle: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Service: Unexpected error updating vehicle {} for {}: {}", vehicleId, userId, e.getMessage(),
					e);
			throw new RuntimeException("An unexpected error occurred while updating vehicle", e);
		}
	}

	public void deleteVehicle(String userId, String vehicleId) {
		try {
			logger.info("Service: Deleting vehicle {} for userId: {}", vehicleId, userId);

			// Validate inputs
			validateUserId(userId);
			validateVehicleId(vehicleId);

			Map<String, Object> userData = manageDAO.findUserData(userId);
			if (userData == null) {
				logger.warn("Service: User not found for userId: {}", userId);
				throw new IllegalArgumentException("User not found: " + userId);
			}

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> vehicles = (List<Map<String, Object>>) userData.get("vehicles");
			if (vehicles == null) {
				logger.warn("Service: No vehicles found for userId: {}", userId);
				throw new IllegalArgumentException("No vehicles found for user: " + userId);
			}

			boolean removed = vehicles.removeIf(vehicle -> vehicleId.equals(vehicle.get("vehicle_id")));

			if (removed) {
				String now = LocalDateTime.now().format(formatter);
				manageDAO.updateField(userId, "vehicles", vehicles);
				manageDAO.updateField(userId, "updated_at", now);
				logger.info("Service: Vehicle {} deleted for userId: {}", vehicleId, userId);
			} else {
				logger.warn("Service: Vehicle {} not found for userId: {}", vehicleId, userId);
				throw new IllegalArgumentException("Vehicle not found: " + vehicleId);
			}

		} catch (IllegalArgumentException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.error("Service: Error deleting vehicle {} for {}: {}", vehicleId, userId, e.getMessage(), e);
			throw new RuntimeException("Failed to delete vehicle: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Service: Unexpected error deleting vehicle {} for {}: {}", vehicleId, userId, e.getMessage(),
					e);
			throw new RuntimeException("An unexpected error occurred while deleting vehicle", e);
		}
	}

	// ============ FAVORITE SPOT OPERATIONS ============

	public void addFavoriteSpot(String userId, Map<String, Object> spot) {
		try {
			logger.info("Service: Adding favorite spot for userId: {}", userId);

			validateUserId(userId);
			validateSpotData(spot);

			Map<String, Object> userData = manageDAO.findUserData(userId);

			if (userData == null) {
				userData = createInitialUserData(userId);
			}

			List<Map<String, Object>> favoriteSpots = getOrCreateList(userData, "favoriteSpots");

			String now = LocalDateTime.now().format(formatter);
			spot.put("created_at", now);
			spot.put("updated_at", now);

			// Check for duplicate spot_id
			String spotId = (String) spot.get("spot_id");
			if (spotId == null || spotId.trim().isEmpty()) {
				throw new IllegalArgumentException("Spot ID is required");
			}

			favoriteSpots.removeIf(s -> spotId.equals(s.get("spot_id")));
			favoriteSpots.add(spot);

			manageDAO.updateField(userId, "favoriteSpots", favoriteSpots);
			manageDAO.updateField(userId, "updated_at", now);

			logger.info("Service: Successfully added favorite spot {} for userId: {}", spotId, userId);

		} catch (IllegalArgumentException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.error("Service: Error adding favorite spot for {}: {}", userId, e.getMessage(), e);
			throw new RuntimeException("Failed to add favorite spot: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Service: Unexpected error adding favorite spot for {}: {}", userId, e.getMessage(), e);
			throw new RuntimeException("An unexpected error occurred while adding favorite spot", e);
		}
	}

	public void updateFavoriteSpot(String userId, String spotId, Map<String, Object> updatedSpot) {
		try {
			logger.info("Service: Updating favorite spot {} for userId: {}", spotId, userId);

			validateUserId(userId);
			validateSpotId(spotId);
			validateSpotData(updatedSpot);

			Map<String, Object> userData = manageDAO.findUserData(userId);
			if (userData == null) {
				throw new IllegalArgumentException("User not found: " + userId);
			}

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> favoriteSpots = (List<Map<String, Object>>) userData.get("favoriteSpots");
			if (favoriteSpots == null) {
				throw new IllegalArgumentException("No favorite spots found for user: " + userId);
			}

			boolean found = false;
			for (Map<String, Object> spot : favoriteSpots) {
				if (spotId.equals(spot.get("spot_id"))) {
					spot.putAll(updatedSpot);
					spot.put("updated_at", LocalDateTime.now().format(formatter));
					found = true;
					break;
				}
			}

			if (!found) {
				throw new IllegalArgumentException("Favorite spot not found: " + spotId);
			}

			String now = LocalDateTime.now().format(formatter);
			manageDAO.updateField(userId, "favoriteSpots", favoriteSpots);
			manageDAO.updateField(userId, "updated_at", now);

		} catch (IllegalArgumentException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.error("Service: Error updating favorite spot {} for {}: {}", spotId, userId, e.getMessage(), e);
			throw new RuntimeException("Failed to update favorite spot: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Service: Unexpected error updating favorite spot {} for {}: {}", spotId, userId,
					e.getMessage(), e);
			throw new RuntimeException("An unexpected error occurred while updating favorite spot", e);
		}
	}

	public void deleteFavoriteSpot(String userId, String spotId) {
		try {
			logger.info("Service: Deleting favorite spot {} for userId: {}", spotId, userId);

			validateUserId(userId);
			validateSpotId(spotId);

			Map<String, Object> userData = manageDAO.findUserData(userId);
			if (userData == null) {
				throw new IllegalArgumentException("User not found: " + userId);
			}

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> favoriteSpots = (List<Map<String, Object>>) userData.get("favoriteSpots");
			if (favoriteSpots == null) {
				throw new IllegalArgumentException("No favorite spots found for user: " + userId);
			}

			boolean removed = favoriteSpots.removeIf(spot -> spotId.equals(spot.get("spot_id")));

			if (!removed) {
				throw new IllegalArgumentException("Favorite spot not found: " + spotId);
			}

			String now = LocalDateTime.now().format(formatter);
			manageDAO.updateField(userId, "favoriteSpots", favoriteSpots);
			manageDAO.updateField(userId, "updated_at", now);

		} catch (IllegalArgumentException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.error("Service: Error deleting favorite spot {} for {}: {}", spotId, userId, e.getMessage(), e);
			throw new RuntimeException("Failed to delete favorite spot: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Service: Unexpected error deleting favorite spot {} for {}: {}", spotId, userId,
					e.getMessage(), e);
			throw new RuntimeException("An unexpected error occurred while deleting favorite spot", e);
		}
	}

	// ============ HISTORY OPERATIONS ============

	public void addHistory(String userId, Map<String, Object> historyItem) {
		try {
			logger.info("Service: Adding history for userId: {}", userId);

			validateUserId(userId);
			validateHistoryData(historyItem);

			Map<String, Object> userData = manageDAO.findUserData(userId);

			if (userData == null) {
				userData = createInitialUserData(userId);
			}

			List<Map<String, Object>> history = getOrCreateList(userData, "history");

			String now = LocalDateTime.now().format(formatter);
			historyItem.put("created_at", now);
			historyItem.put("updated_at", now);

			String historyId = (String) historyItem.get("history_id");
			if (historyId == null || historyId.trim().isEmpty()) {
				throw new IllegalArgumentException("History ID is required");
			}

			history.removeIf(h -> historyId.equals(h.get("history_id")));
			history.add(historyItem);

			manageDAO.updateField(userId, "history", history);
			manageDAO.updateField(userId, "updated_at", now);

		} catch (IllegalArgumentException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.error("Service: Error adding history for {}: {}", userId, e.getMessage(), e);
			throw new RuntimeException("Failed to add history: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Service: Unexpected error adding history for {}: {}", userId, e.getMessage(), e);
			throw new RuntimeException("An unexpected error occurred while adding history", e);
		}
	}

	public void updateHistory(String userId, String historyId, Map<String, Object> updatedHistory) {
		try {
			logger.info("Service: Updating history {} for userId: {}", historyId, userId);

			validateUserId(userId);
			validateHistoryId(historyId);
			validateHistoryData(updatedHistory);

			Map<String, Object> userData = manageDAO.findUserData(userId);
			if (userData == null) {
				throw new IllegalArgumentException("User not found: " + userId);
			}

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> history = (List<Map<String, Object>>) userData.get("history");
			if (history == null) {
				throw new IllegalArgumentException("No history found for user: " + userId);
			}

			boolean found = false;
			for (Map<String, Object> historyItem : history) {
				if (historyId.equals(historyItem.get("history_id"))) {
					historyItem.putAll(updatedHistory);
					historyItem.put("updated_at", LocalDateTime.now().format(formatter));
					found = true;
					break;
				}
			}

			if (!found) {
				throw new IllegalArgumentException("History item not found: " + historyId);
			}

			String now = LocalDateTime.now().format(formatter);
			manageDAO.updateField(userId, "history", history);
			manageDAO.updateField(userId, "updated_at", now);

		} catch (IllegalArgumentException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.error("Service: Error updating history {} for {}: {}", historyId, userId, e.getMessage(), e);
			throw new RuntimeException("Failed to update history: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Service: Unexpected error updating history {} for {}: {}", historyId, userId, e.getMessage(),
					e);
			throw new RuntimeException("An unexpected error occurred while updating history", e);
		}
	}

	public void deleteHistory(String userId, String historyId) {
		try {
			logger.info("Service: Deleting history {} for userId: {}", historyId, userId);

			validateUserId(userId);
			validateHistoryId(historyId);

			Map<String, Object> userData = manageDAO.findUserData(userId);
			if (userData == null) {
				throw new IllegalArgumentException("User not found: " + userId);
			}

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> history = (List<Map<String, Object>>) userData.get("history");
			if (history == null) {
				throw new IllegalArgumentException("No history found for user: " + userId);
			}

			boolean removed = history.removeIf(item -> historyId.equals(item.get("history_id")));

			if (!removed) {
				throw new IllegalArgumentException("History item not found: " + historyId);
			}

			String now = LocalDateTime.now().format(formatter);
			manageDAO.updateField(userId, "history", history);
			manageDAO.updateField(userId, "updated_at", now);

		} catch (IllegalArgumentException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.error("Service: Error deleting history {} for {}: {}", historyId, userId, e.getMessage(), e);
			throw new RuntimeException("Failed to delete history: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Service: Unexpected error deleting history {} for {}: {}", historyId, userId, e.getMessage(),
					e);
			throw new RuntimeException("An unexpected error occurred while deleting history", e);
		}
	}

	// ============ ACTIVE STATUS OPERATIONS ============

	public void addActiveStatus(String userId, Map<String, Object> status) {
		try {
			logger.info("Service: Adding active status for userId: {}", userId);

			validateUserId(userId);
			validateActiveStatusData(status);

			Map<String, Object> userData = manageDAO.findUserData(userId);

			if (userData == null) {
				userData = createInitialUserData(userId);
			}

			List<Map<String, Object>> activeStatus = getOrCreateList(userData, "activeStatus");

			String now = LocalDateTime.now().format(formatter);
			status.put("created_at", now);
			status.put("updated_at", now);

			String statusId = (String) status.get("active_id");
			if (statusId == null || statusId.trim().isEmpty()) {
				throw new IllegalArgumentException("Active status ID is required");
			}

			activeStatus.removeIf(s -> statusId.equals(s.get("active_id")));
			activeStatus.add(status);

			manageDAO.updateField(userId, "activeStatus", activeStatus);
			manageDAO.updateField(userId, "updated_at", now);

		} catch (IllegalArgumentException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.error("Service: Error adding active status for {}: {}", userId, e.getMessage(), e);
			throw new RuntimeException("Failed to add active status: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Service: Unexpected error adding active status for {}: {}", userId, e.getMessage(), e);
			throw new RuntimeException("An unexpected error occurred while adding active status", e);
		}
	}

	public void updateActiveStatus(String userId, String statusId, Map<String, Object> updatedStatus) {
		try {
			logger.info("Service: Updating active status {} for userId: {}", statusId, userId);

			validateUserId(userId);
			validateStatusId(statusId);
			validateActiveStatusData(updatedStatus);

			Map<String, Object> userData = manageDAO.findUserData(userId);
			if (userData == null) {
				throw new IllegalArgumentException("User not found: " + userId);
			}

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> activeStatus = (List<Map<String, Object>>) userData.get("activeStatus");
			if (activeStatus == null) {
				throw new IllegalArgumentException("No active status found for user: " + userId);
			}

			boolean found = false;
			for (Map<String, Object> status : activeStatus) {
				if (statusId.equals(status.get("active_id"))) {
					status.putAll(updatedStatus);
					status.put("updated_at", LocalDateTime.now().format(formatter));
					found = true;
					break;
				}
			}

			if (!found) {
				throw new IllegalArgumentException("Active status not found: " + statusId);
			}

			String now = LocalDateTime.now().format(formatter);
			manageDAO.updateField(userId, "activeStatus", activeStatus);
			manageDAO.updateField(userId, "updated_at", now);

		} catch (IllegalArgumentException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.error("Service: Error updating active status {} for {}: {}", statusId, userId, e.getMessage(), e);
			throw new RuntimeException("Failed to update active status: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Service: Unexpected error updating active status {} for {}: {}", statusId, userId,
					e.getMessage(), e);
			throw new RuntimeException("An unexpected error occurred while updating active status", e);
		}
	}

	public void deleteActiveStatus(String userId, String statusId) {
		try {
			logger.info("Service: Deleting active status {} for userId: {}", statusId, userId);

			validateUserId(userId);
			validateStatusId(statusId);

			Map<String, Object> userData = manageDAO.findUserData(userId);
			if (userData == null) {
				throw new IllegalArgumentException("User not found: " + userId);
			}

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> activeStatus = (List<Map<String, Object>>) userData.get("activeStatus");
			if (activeStatus == null) {
				throw new IllegalArgumentException("No active status found for user: " + userId);
			}

			boolean removed = activeStatus.removeIf(status -> statusId.equals(status.get("active_id")));

			if (!removed) {
				throw new IllegalArgumentException("Active status not found: " + statusId);
			}

			String now = LocalDateTime.now().format(formatter);
			manageDAO.updateField(userId, "activeStatus", activeStatus);
			manageDAO.updateField(userId, "updated_at", now);

		} catch (IllegalArgumentException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.error("Service: Error deleting active status {} for {}: {}", statusId, userId, e.getMessage(), e);
			throw new RuntimeException("Failed to delete active status: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Service: Unexpected error deleting active status {} for {}: {}", statusId, userId,
					e.getMessage(), e);
			throw new RuntimeException("An unexpected error occurred while deleting active status", e);
		}
	}

	// ============ VALIDATION METHODS ============

	private void validateUserId(String userId) {
		if (userId == null || userId.trim().isEmpty()) {
			throw new IllegalArgumentException("User ID cannot be null or empty");
		}
	}

	private void validateVehicleId(String vehicleId) {
		if (vehicleId == null || vehicleId.trim().isEmpty()) {
			throw new IllegalArgumentException("Vehicle ID cannot be null or empty");
		}
	}

	private void validateVehicleData(Map<String, Object> vehicle) {
		if (vehicle == null || vehicle.isEmpty()) {
			throw new IllegalArgumentException("Vehicle data cannot be null or empty");
		}
		if (!vehicle.containsKey("vehicle_id")) {
			throw new IllegalArgumentException("Vehicle data must contain 'vehicle_id'");
		}
	}

	private void validateSpotId(String spotId) {
		if (spotId == null || spotId.trim().isEmpty()) {
			throw new IllegalArgumentException("Spot ID cannot be null or empty");
		}
	}

	private void validateSpotData(Map<String, Object> spot) {
		if (spot == null || spot.isEmpty()) {
			throw new IllegalArgumentException("Spot data cannot be null or empty");
		}
		if (!spot.containsKey("spot_id")) {
			throw new IllegalArgumentException("Spot data must contain 'spot_id'");
		}
	}

	private void validateHistoryId(String historyId) {
		if (historyId == null || historyId.trim().isEmpty()) {
			throw new IllegalArgumentException("History ID cannot be null or empty");
		}
	}

	private void validateHistoryData(Map<String, Object> history) {
		if (history == null || history.isEmpty()) {
			throw new IllegalArgumentException("History data cannot be null or empty");
		}
		if (!history.containsKey("history_id")) {
			throw new IllegalArgumentException("History data must contain 'history_id'");
		}
	}

	private void validateStatusId(String statusId) {
		if (statusId == null || statusId.trim().isEmpty()) {
			throw new IllegalArgumentException("Status ID cannot be null or empty");
		}
	}

	private void validateActiveStatusData(Map<String, Object> status) {
		if (status == null || status.isEmpty()) {
			throw new IllegalArgumentException("Status data cannot be null or empty");
		}
		if (!status.containsKey("active_id")) {
			throw new IllegalArgumentException("Status data must contain 'active_id'");
		}
	}

	// ============ HELPER METHODS ============

	private Map<String, Object> createInitialUserData(String userId) {
		Map<String, Object> userData = new HashMap<>();
		userData.put("_id", UUID.randomUUID().toString());
		userData.put("user_id", userId);
		userData.put("vehicles", new ArrayList<>());
		userData.put("favoriteSpots", new ArrayList<>());
		userData.put("activeStatus", new ArrayList<>());
		userData.put("history", new ArrayList<>());

		String now = LocalDateTime.now().format(formatter);
		userData.put("created_at", now);
		userData.put("updated_at", now);

		return userData;
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getOrCreateList(Map<String, Object> userData, String key) {
		List<Map<String, Object>> list = (List<Map<String, Object>>) userData.get(key);
		if (list == null) {
			list = new ArrayList<>();
			userData.put(key, list);
		}
		return list;
	}

	// Convert MongoDB document to ManageData (for API response)
	@SuppressWarnings("unchecked")
	private ManageData convertToManageData(Map<String, Object> dbData) {
		try {
			String userId = (String) dbData.get("user_id");
			if (userId == null) {
				throw new IllegalArgumentException("User ID not found in database document");
			}

			ManageData manageData = new ManageData(userId);

			// Convert arrays to maps for ManageData structure
			convertListToMap((List<Map<String, Object>>) dbData.get("vehicles"), manageData.getVehicles(),
					"vehicle_id");
			convertListToMap((List<Map<String, Object>>) dbData.get("favoriteSpots"), manageData.getFavoriteSpots(),
					"spot_id");
			convertListToMap((List<Map<String, Object>>) dbData.get("activeStatus"), manageData.getActiveStatus(),
					"active_id");
			convertListToMap((List<Map<String, Object>>) dbData.get("history"), manageData.getHistory(), "history_id");

			return manageData;
		} catch (Exception e) {
			logger.error("Service: Error converting database data to ManageData: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to convert database data", e);
		}
	}

	private void convertListToMap(List<Map<String, Object>> list, Map<String, Map<String, Object>> targetMap,
			String idField) {
		if (list != null) {
			for (Map<String, Object> item : list) {
				try {
					String id = (String) item.get(idField);
					if (id != null) {
						targetMap.put(id, item);
					}
				} catch (Exception e) {
					logger.warn("Service: Skipping item during conversion - missing ID field: {}", idField);
				}
			}
		}
	}
}