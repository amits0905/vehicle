package com.park_karo.vehicle.manage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "manage_data")
public class ManageData {

	private final String userId;

	// Sections stored as Map-of-Maps for quick access by ID
	private final Map<String, Map<String, Object>> vehicles = new HashMap<>();
	private final Map<String, Map<String, Object>> favoriteSpots = new HashMap<>();
	private final Map<String, Map<String, Object>> history = new HashMap<>();
	private final Map<String, Map<String, Object>> activeStatus = new HashMap<>();

	// ---------------------------------------
	// Constructor 1: only userId
	public ManageData(String userId) {
		this.userId = userId;
	}

	// Constructor 2: userId + lists (for legacy service code)
	public ManageData(String userId, List<Map<String, Object>> vehiclesList,
			List<Map<String, Object>> favoriteSpotsList, List<Map<String, Object>> activeStatusList,
			List<Map<String, Object>> historyList) {

		this.userId = userId;

		if (vehiclesList != null) {
			vehiclesList.forEach(v -> vehicles.put(v.get("vehicle_id").toString(), v));
		}

		if (favoriteSpotsList != null) {
			favoriteSpotsList.forEach(s -> favoriteSpots.put(s.get("spot_id").toString(), s));
		}

		if (activeStatusList != null) {
			activeStatusList.forEach(a -> activeStatus.put(a.get("active_id").toString(), a));
		}

		if (historyList != null) {
			historyList.forEach(h -> history.put(h.get("history_id").toString(), h));
		}
	}

	// ---------------------------------------
	// Getters for Jackson serialization
	public String getUserId() {
		return userId;
	}

	public Map<String, Map<String, Object>> getVehicles() {
		return vehicles;
	}

	public Map<String, Map<String, Object>> getFavoriteSpots() {
		return favoriteSpots;
	}

	public Map<String, Map<String, Object>> getHistory() {
		return history;
	}

	public Map<String, Map<String, Object>> getActiveStatus() {
		return activeStatus;
	}

	// ---------------------------------------
	// Generic helper to get section by name
	private Map<String, Map<String, Object>> getSection(String section) {
		return switch (section) {
		case "vehicles" -> vehicles;
		case "favoriteSpots" -> favoriteSpots;
		case "history" -> history;
		case "activeStatus" -> activeStatus;
		default -> throw new IllegalArgumentException("Unknown section: " + section);
		};
	}

	// ---------------------------------------
	// CRUD operations on sections
	public void addToSection(String section, Map<String, Object> item) {
		Map<String, Map<String, Object>> targetSection = getSection(section);
		String itemId = extractItemId(section, item);
		targetSection.put(itemId, item);
	}

	public void updateInSection(String section, String itemId, Map<String, Object> updatedItem) {
		Map<String, Map<String, Object>> targetSection = getSection(section);
		if (targetSection.containsKey(itemId)) {
			targetSection.put(itemId, updatedItem);
		}
	}

	public void deleteFromSection(String section, String itemId) {
		Map<String, Map<String, Object>> targetSection = getSection(section);
		targetSection.remove(itemId);
	}

	// ---------------------------------------
	// Helper to extract ID depending on section
	private String extractItemId(String section, Map<String, Object> item) {
		return switch (section) {
		case "vehicles" -> item.get("vehicle_id").toString();
		case "favoriteSpots" -> item.get("spot_id").toString();
		case "history" -> item.get("history_id").toString();
		case "activeStatus" -> item.get("active_id").toString();
		default -> throw new IllegalArgumentException("Unknown section: " + section);
		};
	}
}
