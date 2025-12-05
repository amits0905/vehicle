package com.park_karo.vehicle.manage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ManageService {

	private static final Logger logger = LoggerFactory.getLogger(ManageService.class);

	private final ManageDAO manageDAO;
	private final Executor executor;

	public ManageService(ManageDAO manageDAO, Executor executor) {
		this.manageDAO = manageDAO;
		this.executor = executor;
	}

	// ======================= SYNC METHODS =======================

	public void addVehicle(String userId, Map<String, Object> vehicle) {
		manageDAO.addVehicle(userId, vehicle);
	}

	public void updateVehicle(String userId, String vehicleId, Map<String, Object> vehicle) {
		manageDAO.updateVehicle(userId, vehicleId, vehicle);
	}

	public void deleteVehicle(String userId, String vehicleId) {
		manageDAO.deleteVehicle(userId, vehicleId);
	}

	public void addFavoriteSpot(String userId, Map<String, Object> spot) {
		manageDAO.addFavoriteSpot(userId, spot);
	}

	public void updateFavoriteSpot(String userId, String spotId, Map<String, Object> spot) {
		manageDAO.updateFavoriteSpot(userId, spotId, spot);
	}

	public void deleteFavoriteSpot(String userId, String spotId) {
		manageDAO.deleteFavoriteSpot(userId, spotId);
	}

	public void addHistory(String userId, Map<String, Object> historyItem) {
		manageDAO.addHistory(userId, historyItem);
	}

	public void updateHistory(String userId, String historyId, Map<String, Object> historyItem) {
		manageDAO.updateHistory(userId, historyId, historyItem);
	}

	public void deleteHistory(String userId, String historyId) {
		manageDAO.deleteHistory(userId, historyId);
	}

	public void addActiveStatus(String userId, Map<String, Object> status) {
		manageDAO.addActiveStatus(userId, status);
	}

	public void updateActiveStatus(String userId, String statusId, Map<String, Object> status) {
		manageDAO.updateActiveStatus(userId, statusId, status);
	}

	public void deleteActiveStatus(String userId, String statusId) {
		manageDAO.deleteActiveStatus(userId, statusId);
	}

	/**
	 * Synchronous: Get full ManageData for a single user
	 */
	@SuppressWarnings("unchecked")
	public ManageData getManageData(String userId) {
		logger.info("SERVICE: getManageData called for {}", userId);

		Map<String, Object> data = manageDAO.findUserData(userId);
		if (data == null) {
			return new ManageData(userId, List.of(), List.of(), List.of(), List.of());
		}

		return new ManageData(userId, (List<Map<String, Object>>) data.getOrDefault("vehicles", List.of()),
				(List<Map<String, Object>>) data.getOrDefault("favoriteSpots", List.of()),
				(List<Map<String, Object>>) data.getOrDefault("activeStatus", List.of()),
				(List<Map<String, Object>>) data.getOrDefault("history", List.of()));
	}

	// ======================= ASYNC METHODS =======================

	/**
	 * Async: Get multiple users' ManageData
	 */
	@Async
	public CompletableFuture<Map<String, ManageData>> getMultipleUsersDataAsync(List<String> userIds) {
		return CompletableFuture
				.supplyAsync(() -> userIds.stream().collect(Collectors.toMap(id -> id, this::getManageData)), executor);
	}

	/**
	 * Async: Batch add vehicles for multiple users
	 */
	@Async
	public CompletableFuture<Map<String, List<String>>> batchAddVehiclesAsync(
			Map<String, List<Map<String, Object>>> userVehiclesMap) {

		return CompletableFuture.supplyAsync(() -> {
			Map<String, List<String>> results = new HashMap<>();

			userVehiclesMap.forEach((userId, vehicles) -> {
				List<String> added = new ArrayList<>();
				for (Map<String, Object> v : vehicles) {
					addVehicle(userId, v);
					added.add((String) v.get("vehicle_id"));
				}
				results.put(userId, added);
			});

			return results;
		}, executor);
	}

	/**
	 * Async: Batch update vehicles for a single user
	 */
	@Async
	public CompletableFuture<List<String>> batchUpdateVehiclesAsync(String userId,
			Map<String, Map<String, Object>> vehicleUpdates) {

		return CompletableFuture.supplyAsync(() -> {
			List<String> updated = new ArrayList<>();

			vehicleUpdates.forEach((vehicleId, data) -> {
				updateVehicle(userId, vehicleId, data);
				updated.add(vehicleId);
			});

			return updated;
		}, executor);
	}

	/**
	 * Async: Generate a simple user report
	 */
	@Async
	public CompletableFuture<Map<String, Object>> generateUserReportAsync(List<String> userIds) {
		return CompletableFuture.supplyAsync(() -> {

			Map<String, Object> report = new HashMap<>();
			List<Map<String, Object>> userReports = new ArrayList<>();

			for (String userId : userIds) {
				ManageData data = getManageData(userId);

				Map<String, Object> summary = new HashMap<>();
				summary.put("userId", userId);
				summary.put("vehicles", data.getVehicles().size());
				summary.put("favoriteSpots", data.getFavoriteSpots().size());
				summary.put("activeStatus", data.getActiveStatus().size());
				summary.put("history", data.getHistory().size());

				userReports.add(summary);
			}

			report.put("generated_at", Instant.now().toString());
			report.put("users", userReports);

			return report;

		}, executor);
	}
}
