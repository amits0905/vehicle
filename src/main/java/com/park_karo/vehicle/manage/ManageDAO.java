package com.park_karo.vehicle.manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class ManageDAO {

	private static final Logger logger = LoggerFactory.getLogger(ManageDAO.class);
	private final MongoTemplate mongoTemplate;
	private static final String COLLECTION_NAME = "manage_data";

	public ManageDAO(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	// ================= BASIC CRUD =================

	public Map<String, Object> findUserData(String userId) {
		try {
			Query query = new Query(Criteria.where("user_id").is(userId));
			@SuppressWarnings("unchecked")
			Map<String, Object> result = mongoTemplate.findOne(query, Map.class, COLLECTION_NAME);
			return result;
		} catch (Exception e) {
			logger.error("Error finding user data for userId {}: {}", userId, e.getMessage(), e);
			return null;
		}
	}

	public void saveUserData(String userId, Map<String, Object> data) {
		try {
			data.put("user_id", userId);
			mongoTemplate.save(data, COLLECTION_NAME);
		} catch (Exception e) {
			logger.error("Error saving user data for userId {}: {}", userId, e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public void updateField(String userId, String field, Object value) {
		try {
			Query query = new Query(Criteria.where("user_id").is(userId));
			Update update = new Update().set(field, value);
			mongoTemplate.upsert(query, update, Map.class, COLLECTION_NAME);
		} catch (Exception e) {
			logger.error("Error updating field {} for userId {}: {}", field, userId, e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public void deleteUserData(String userId) {
		try {
			Query query = new Query(Criteria.where("user_id").is(userId));
			mongoTemplate.remove(query, COLLECTION_NAME);
		} catch (Exception e) {
			logger.error("Error deleting user data for userId {}: {}", userId, e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	// ================= VEHICLES =================

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getUserVehicles(String userId) {
		Map<String, Object> userData = findUserData(userId);
		if (userData != null && userData.containsKey("vehicles")) {
			return (List<Map<String, Object>>) userData.get("vehicles");
		}
		return new ArrayList<>();
	}

	public void addVehicle(String userId, Map<String, Object> vehicle) {
		Map<String, Object> userData = findUserData(userId);
		if (userData == null)
			userData = createInitialUserData(userId);

		List<Map<String, Object>> vehicles = getOrCreateList(userData, "vehicles");
		String vehicleId = (String) vehicle.get("vehicle_id");
		vehicles.removeIf(v -> vehicleId != null && vehicleId.equals(v.get("vehicle_id")));
		vehicles.add(vehicle);

		saveUserData(userId, userData);
	}

	public void updateVehicle(String userId, String vehicleId, Map<String, Object> updatedVehicle) {
		Map<String, Object> userData = findUserData(userId);
		if (userData == null)
			return;

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> vehicles = (List<Map<String, Object>>) userData.get("vehicles");
		if (vehicles == null)
			return;

		for (int i = 0; i < vehicles.size(); i++) {
			Map<String, Object> vehicle = vehicles.get(i);
			if (vehicleId.equals(vehicle.get("vehicle_id"))) {
				updatedVehicle.put("vehicle_id", vehicleId);
				vehicles.set(i, updatedVehicle);
				break;
			}
		}
		saveUserData(userId, userData);
	}

	public void deleteVehicle(String userId, String vehicleId) {
		Map<String, Object> userData = findUserData(userId);
		if (userData == null)
			return;

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> vehicles = (List<Map<String, Object>>) userData.get("vehicles");
		if (vehicles == null)
			return;

		vehicles.removeIf(v -> vehicleId.equals(v.get("vehicle_id")));
		saveUserData(userId, userData);
	}

	// ================= FAVORITE SPOTS =================

	public void addFavoriteSpot(String userId, Map<String, Object> spot) {
		Map<String, Object> userData = findUserData(userId);
		if (userData == null)
			userData = createInitialUserData(userId);

		List<Map<String, Object>> spots = getOrCreateList(userData, "favoriteSpots");
		String spotId = (String) spot.get("spot_id");
		spots.removeIf(s -> spotId != null && spotId.equals(s.get("spot_id")));
		spots.add(spot);

		saveUserData(userId, userData);
	}

	public void updateFavoriteSpot(String userId, String spotId, Map<String, Object> updatedSpot) {
		Map<String, Object> userData = findUserData(userId);
		if (userData == null)
			return;

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> spots = (List<Map<String, Object>>) userData.get("favoriteSpots");
		if (spots == null)
			return;

		for (int i = 0; i < spots.size(); i++) {
			Map<String, Object> spot = spots.get(i);
			if (spotId.equals(spot.get("spot_id"))) {
				updatedSpot.put("spot_id", spotId);
				spots.set(i, updatedSpot);
				break;
			}
		}
		saveUserData(userId, userData);
	}

	public void deleteFavoriteSpot(String userId, String spotId) {
		Map<String, Object> userData = findUserData(userId);
		if (userData == null)
			return;

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> spots = (List<Map<String, Object>>) userData.get("favoriteSpots");
		if (spots == null)
			return;

		spots.removeIf(s -> spotId.equals(s.get("spot_id")));
		saveUserData(userId, userData);
	}

	// ================= HISTORY =================

	public void addHistory(String userId, Map<String, Object> historyItem) {
		Map<String, Object> userData = findUserData(userId);
		if (userData == null)
			userData = createInitialUserData(userId);

		List<Map<String, Object>> history = getOrCreateList(userData, "history");
		String historyId = (String) historyItem.get("history_id");
		history.removeIf(h -> historyId != null && historyId.equals(h.get("history_id")));
		history.add(historyItem);

		saveUserData(userId, userData);
	}

	public void updateHistory(String userId, String historyId, Map<String, Object> updatedHistory) {
		Map<String, Object> userData = findUserData(userId);
		if (userData == null)
			return;

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> history = (List<Map<String, Object>>) userData.get("history");
		if (history == null)
			return;

		for (int i = 0; i < history.size(); i++) {
			Map<String, Object> h = history.get(i);
			if (historyId.equals(h.get("history_id"))) {
				updatedHistory.put("history_id", historyId);
				history.set(i, updatedHistory);
				break;
			}
		}
		saveUserData(userId, userData);
	}

	public void deleteHistory(String userId, String historyId) {
		Map<String, Object> userData = findUserData(userId);
		if (userData == null)
			return;

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> history = (List<Map<String, Object>>) userData.get("history");
		if (history == null)
			return;

		history.removeIf(h -> historyId.equals(h.get("history_id")));
		saveUserData(userId, userData);
	}

	// ================= ACTIVE STATUS =================

	public void addActiveStatus(String userId, Map<String, Object> status) {
		Map<String, Object> userData = findUserData(userId);
		if (userData == null)
			userData = createInitialUserData(userId);

		List<Map<String, Object>> activeStatus = getOrCreateList(userData, "activeStatus");
		String statusId = (String) status.get("active_id");
		activeStatus.removeIf(s -> statusId != null && statusId.equals(s.get("active_id")));
		activeStatus.add(status);

		saveUserData(userId, userData);
	}

	public void updateActiveStatus(String userId, String statusId, Map<String, Object> updatedStatus) {
		Map<String, Object> userData = findUserData(userId);
		if (userData == null)
			return;

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> activeStatus = (List<Map<String, Object>>) userData.get("activeStatus");
		if (activeStatus == null)
			return;

		for (int i = 0; i < activeStatus.size(); i++) {
			Map<String, Object> s = activeStatus.get(i);
			if (statusId.equals(s.get("active_id"))) {
				updatedStatus.put("active_id", statusId);
				activeStatus.set(i, updatedStatus);
				break;
			}
		}
		saveUserData(userId, userData);
	}

	public void deleteActiveStatus(String userId, String statusId) {
		Map<String, Object> userData = findUserData(userId);
		if (userData == null)
			return;

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> activeStatus = (List<Map<String, Object>>) userData.get("activeStatus");
		if (activeStatus == null)
			return;

		activeStatus.removeIf(s -> statusId.equals(s.get("active_id")));
		saveUserData(userId, userData);
	}

	// ================= HELPER METHODS =================

	private Map<String, Object> createInitialUserData(String userId) {
		Map<String, Object> userData = new HashMap<>();
		userData.put("user_id", userId);
		userData.put("vehicles", new ArrayList<Map<String, Object>>());
		userData.put("favoriteSpots", new ArrayList<Map<String, Object>>());
		userData.put("activeStatus", new ArrayList<Map<String, Object>>());
		userData.put("history", new ArrayList<Map<String, Object>>());
		userData.put("created_at", java.time.Instant.now().toString());
		userData.put("updated_at", java.time.Instant.now().toString());
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
}
