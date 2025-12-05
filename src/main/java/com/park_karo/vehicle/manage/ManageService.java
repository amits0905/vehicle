package com.park_karo.vehicle.manage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.park_karo.vehicle.exception.CustomExceptions.ResourceNotFoundBusinessException;
import com.park_karo.vehicle.exception.CustomExceptions.ValidationBusinessException;
import com.park_karo.vehicle.dto.VehicleDTO;

@Service
public class ManageService {

	private static final Logger logger = LoggerFactory.getLogger(ManageService.class);

	private final ManageDAO manageDAO;
	private final Executor executor;

	public ManageService(ManageDAO manageDAO, Executor executor) {
		this.manageDAO = manageDAO;
		this.executor = executor;
	}

	// ------------------- Sync Methods -------------------
	public void addVehicle(String userId, Map<String, Object> vehicle) {
		validateVehicleData(vehicle);
		manageDAO.addVehicle(userId, vehicle);
	}

	public void updateVehicle(String userId, String vehicleId, Map<String, Object> vehicle) {
		validateVehicleData(vehicle);
		if (!vehicleId.equals(vehicle.get("vehicle_id"))) {
			throw new ValidationBusinessException("vehicle_id", "Vehicle ID in path doesn't match request body");
		}
		manageDAO.updateVehicle(userId, vehicleId, vehicle);
	}

	public void deleteVehicle(String userId, String vehicleId) {
		manageDAO.deleteVehicle(userId, vehicleId);
	}

	public void addFavoriteSpot(String userId, Map<String, Object> spot) {
		validateFavoriteSpotData(spot);
		manageDAO.addFavoriteSpot(userId, spot);
	}

	public void updateFavoriteSpot(String userId, String spotId, Map<String, Object> spot) {
		validateFavoriteSpotData(spot);
		if (!spotId.equals(spot.get("spot_id"))) {
			throw new ValidationBusinessException("spot_id", "Spot ID in path doesn't match request body");
		}
		manageDAO.updateFavoriteSpot(userId, spotId, spot);
	}

	public void deleteFavoriteSpot(String userId, String spotId) {
		manageDAO.deleteFavoriteSpot(userId, spotId);
	}

	public void addHistory(String userId, Map<String, Object> historyItem) {
		validateHistoryData(historyItem);
		manageDAO.addHistory(userId, historyItem);
	}

	public void updateHistory(String userId, String historyId, Map<String, Object> historyItem) {
		validateHistoryData(historyItem);
		if (!historyId.equals(historyItem.get("history_id"))) {
			throw new ValidationBusinessException("history_id", "History ID in path doesn't match request body");
		}
		manageDAO.updateHistory(userId, historyId, historyItem);
	}

	public void deleteHistory(String userId, String historyId) {
		manageDAO.deleteHistory(userId, historyId);
	}

	public void addActiveStatus(String userId, Map<String, Object> status) {
		validateActiveStatusData(status);
		manageDAO.addActiveStatus(userId, status);
	}

	public void updateActiveStatus(String userId, String statusId, Map<String, Object> status) {
		validateActiveStatusData(status);
		if (!statusId.equals(status.get("active_id"))) {
			throw new ValidationBusinessException("active_id", "Active status ID in path doesn't match request body");
		}
		manageDAO.updateActiveStatus(userId, statusId, status);
	}

	public void deleteActiveStatus(String userId, String statusId) {
		manageDAO.deleteActiveStatus(userId, statusId);
	}

	@SuppressWarnings("unchecked")
	public ManageData getManageData(String userId) {
		logger.info("SERVICE: getManageData called for {}", userId);
		Map<String, Object> data = manageDAO.findUserData(userId);
		if (data == null) {
			throw new ResourceNotFoundBusinessException("User data", userId);
		}
		return new ManageData(userId, (List<Map<String, Object>>) data.getOrDefault("vehicles", List.of()),
				(List<Map<String, Object>>) data.getOrDefault("favoriteSpots", List.of()),
				(List<Map<String, Object>>) data.getOrDefault("activeStatus", List.of()),
				(List<Map<String, Object>>) data.getOrDefault("history", List.of()));
	}

	// ------------------- Validation Methods -------------------
	private void validateVehicleData(Map<String, Object> vehicle) {
		if (vehicle == null) {
			throw new ValidationBusinessException("vehicle", "Vehicle data cannot be null");
		}
		if (!vehicle.containsKey("vehicle_id") || vehicle.get("vehicle_id") == null) {
			throw new ValidationBusinessException("vehicle_id", "Vehicle ID is required");
		}
	}

	private void validateFavoriteSpotData(Map<String, Object> spot) {
		if (spot == null) {
			throw new ValidationBusinessException("spot", "Favorite spot data cannot be null");
		}
		if (!spot.containsKey("spot_id") || spot.get("spot_id") == null) {
			throw new ValidationBusinessException("spot_id", "Spot ID is required");
		}
	}

	private void validateHistoryData(Map<String, Object> historyItem) {
		if (historyItem == null) {
			throw new ValidationBusinessException("history", "History data cannot be null");
		}
		if (!historyItem.containsKey("history_id") || historyItem.get("history_id") == null) {
			throw new ValidationBusinessException("history_id", "History ID is required");
		}
	}

	private void validateActiveStatusData(Map<String, Object> status) {
		if (status == null) {
			throw new ValidationBusinessException("status", "Active status data cannot be null");
		}
		if (!status.containsKey("active_id") || status.get("active_id") == null) {
			throw new ValidationBusinessException("active_id", "Active status ID is required");
		}
	}

	// ------------------- Async Methods -------------------
	@Async
	public CompletableFuture<Map<String, ManageData>> getMultipleUsersDataAsync(List<String> userIds) {
		return CompletableFuture.supplyAsync(() -> {
			Map<String, ManageData> result = new HashMap<>();
			for (String userId : userIds) {
				try {
					result.put(userId, getManageData(userId));
				} catch (ResourceNotFoundBusinessException e) {
					logger.warn("User {} not found: {}", userId, e.getMessage());
				}
			}
			return result;
		}, executor);
	}

	@Async
	public CompletableFuture<Map<String, List<String>>> batchAddVehiclesAsync(
			Map<String, List<VehicleDTO>> userVehiclesMap) {

		return CompletableFuture.supplyAsync(() -> {
			Map<String, List<String>> results = new HashMap<>();
			userVehiclesMap.forEach((userId, vehicleDTOs) -> {
				List<String> added = new ArrayList<>();
				for (VehicleDTO dto : vehicleDTOs) {
					try {
						Map<String, Object> vehicleMap = dto.toMap();
						addVehicle(userId, vehicleMap);
						added.add((String) vehicleMap.get("vehicle_id"));
					} catch (Exception e) {
						logger.error("Failed to add vehicle for user {}: {}", userId, e.getMessage());
					}
				}
				results.put(userId, added);
			});
			return results;
		}, executor);
	}

	@Async
	public CompletableFuture<List<String>> batchUpdateVehiclesAsync(String userId,
			Map<String, VehicleDTO> vehicleUpdates) {

		return CompletableFuture.supplyAsync(() -> {
			List<String> updated = new ArrayList<>();
			vehicleUpdates.forEach((vehicleId, dto) -> {
				try {
					Map<String, Object> vehicleMap = dto.toMap();
					updateVehicle(userId, vehicleId, vehicleMap);
					updated.add(vehicleId);
				} catch (Exception e) {
					logger.error("Failed to update vehicle {} for user {}: {}", vehicleId, userId, e.getMessage());
				}
			});
			return updated;
		}, executor);
	}

	@Async
	public CompletableFuture<Map<String, Object>> generateUserReportAsync(List<String> userIds) {
		return CompletableFuture.supplyAsync(() -> {
			Map<String, Object> report = new HashMap<>();
			List<Map<String, Object>> userReports = new ArrayList<>();
			int totalVehicles = 0;
			int totalSpots = 0;
			int totalActiveStatus = 0;
			int totalHistory = 0;

			for (String userId : userIds) {
				try {
					ManageData data = getManageData(userId);
					Map<String, Object> summary = new HashMap<>();
					summary.put("userId", userId);
					summary.put("vehicles", data.getVehicles().size());
					summary.put("favoriteSpots", data.getFavoriteSpots().size());
					summary.put("activeStatus", data.getActiveStatus().size());
					summary.put("history", data.getHistory().size());
					userReports.add(summary);

					totalVehicles += data.getVehicles().size();
					totalSpots += data.getFavoriteSpots().size();
					totalActiveStatus += data.getActiveStatus().size();
					totalHistory += data.getHistory().size();
				} catch (ResourceNotFoundBusinessException e) {
					Map<String, Object> errorSummary = new HashMap<>();
					errorSummary.put("userId", userId);
					errorSummary.put("error", "User not found");
					errorSummary.put("message", e.getMessage());
					userReports.add(errorSummary);
				}
			}

			report.put("generated_at", Instant.now().toString());
			report.put("total_users", userIds.size());
			report.put("successful_users", userReports.stream().filter(r -> !r.containsKey("error")).count());
			report.put("failed_users", userReports.stream().filter(r -> r.containsKey("error")).count());
			report.put("total_vehicles", totalVehicles);
			report.put("total_favorite_spots", totalSpots);
			report.put("total_active_status", totalActiveStatus);
			report.put("total_history", totalHistory);
			report.put("users", userReports);

			return report;
		}, executor);
	}
}