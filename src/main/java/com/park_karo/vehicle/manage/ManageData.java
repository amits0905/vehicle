package com.park_karo.vehicle.manage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

import com.park_karo.vehicle.exception.CustomExceptions.ValidationBusinessException;

@Document(collection = "manage_data")
public class ManageData {

	private final String userId;

	private final Map<String, Map<String, Object>> vehicles = new HashMap<>();
	private final Map<String, Map<String, Object>> favoriteSpots = new HashMap<>();
	private final Map<String, Map<String, Object>> history = new HashMap<>();
	private final Map<String, Map<String, Object>> activeStatus = new HashMap<>();

	// -----------------------
	public ManageData(String userId) {
		this.userId = userId;
	}

	public ManageData(String userId, List<Map<String, Object>> vehiclesList,
			List<Map<String, Object>> favoriteSpotsList, List<Map<String, Object>> activeStatusList,
			List<Map<String, Object>> historyList) {

		this.userId = userId;

		if (vehiclesList != null) {
			vehiclesList.forEach(v -> {
				if (v.get("vehicle_id") != null) {
					vehicles.put(v.get("vehicle_id").toString(), v);
				}
			});
		}

		if (favoriteSpotsList != null) {
			favoriteSpotsList.forEach(s -> {
				if (s.get("spot_id") != null) {
					favoriteSpots.put(s.get("spot_id").toString(), s);
				}
			});
		}

		if (activeStatusList != null) {
			activeStatusList.forEach(a -> {
				if (a.get("active_id") != null) {
					activeStatus.put(a.get("active_id").toString(), a);
				}
			});
		}

		if (historyList != null) {
			historyList.forEach(h -> {
				if (h.get("history_id") != null) {
					history.put(h.get("history_id").toString(), h);
				}
			});
		}
	}

	// -----------------------
	public String getUserId() {
		return userId;
	}

	public Map<String, Map<String, Object>> getVehicles() {
		return new HashMap<>(vehicles); // Return copy for immutability
	}

	public Map<String, Map<String, Object>> getFavoriteSpots() {
		return new HashMap<>(favoriteSpots);
	}

	public Map<String, Map<String, Object>> getHistory() {
		return new HashMap<>(history);
	}

	public Map<String, Map<String, Object>> getActiveStatus() {
		return new HashMap<>(activeStatus);
	}

	// -----------------------
	private Map<String, Map<String, Object>> getSection(String section) {
		return switch (section) {
		case "vehicles" -> vehicles;
		case "favoriteSpots" -> favoriteSpots;
		case "history" -> history;
		case "activeStatus" -> activeStatus;
		default -> throw new ValidationBusinessException("section", "Unknown section: " + section);
		};
	}

	// -----------------------
	public void addToSection(String section, Map<String, Object> item) {
		Map<String, Map<String, Object>> targetSection = getSection(section);
		String itemId = extractItemId(section, item);
		targetSection.put(itemId, item);
	}

	public void updateInSection(String section, String itemId, Map<String, Object> updatedItem) {
		Map<String, Map<String, Object>> targetSection = getSection(section);
		if (!targetSection.containsKey(itemId)) {
			throw new ValidationBusinessException("itemId",
					String.format("%s with ID %s not found in section %s", getItemType(section), itemId, section));
		}
		targetSection.put(itemId, updatedItem);
	}

	public void deleteFromSection(String section, String itemId) {
		Map<String, Map<String, Object>> targetSection = getSection(section);
		if (!targetSection.containsKey(itemId)) {
			throw new ValidationBusinessException("itemId",
					String.format("%s with ID %s not found in section %s", getItemType(section), itemId, section));
		}
		targetSection.remove(itemId);
	}

	// -----------------------
	private String extractItemId(String section, Map<String, Object> item) {
		Object itemId = switch (section) {
		case "vehicles" -> item.get("vehicle_id");
		case "favoriteSpots" -> item.get("spot_id");
		case "history" -> item.get("history_id");
		case "activeStatus" -> item.get("active_id");
		default -> throw new ValidationBusinessException("section", "Unknown section: " + section);
		};

		if (itemId == null) {
			throw new ValidationBusinessException(getItemType(section) + "_id",
					String.format("%s ID is required", getItemType(section)));
		}

		return itemId.toString();
	}

	private String getItemType(String section) {
		return switch (section) {
		case "vehicles" -> "Vehicle";
		case "favoriteSpots" -> "Favorite spot";
		case "history" -> "History item";
		case "activeStatus" -> "Active status";
		default -> "Item";
		};
	}
}