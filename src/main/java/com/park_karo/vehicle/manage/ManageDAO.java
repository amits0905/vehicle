package com.park_karo.vehicle.manage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

@Repository // Now applied to a concrete class
public class ManageDAO { // <--- CHANGED FROM 'interface' TO 'class'

	// 1. Logger declaration (with a name)
	private static final Logger logger = LoggerFactory.getLogger(ManageDAO.class);

	// 2. Dependency injection via constructor (modern Spring practice)
	private final MongoTemplate mongoTemplate;
    
    // 3. Constant declaration
	private static final String COLLECTION_NAME = "manage_data";

    // Constructor for injection (replaces @Autowired field injection)
	public ManageDAO(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	// ============ BASIC CRUD OPERATIONS ============

	/**
	 * Find user data by userId
	 */
	public Map<String, Object> findUserData(String userId) {
		try {
			logger.info("DAO: Finding user data for userId: {}", userId);
			Query query = new Query(Criteria.where("_id").is(userId));
			@SuppressWarnings("unchecked")
			Map<String, Object> result = mongoTemplate.findOne(query, Map.class, COLLECTION_NAME);

			if (result != null) {
				logger.info("DAO: Found user data for userId: {}", userId);
			} else {
				logger.info("DAO: No data found for userId: {}", userId);
			}

			return result;
		} catch (Exception e) {
			logger.error("DAO: Error finding user data for userId: {} - {}", userId, e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Save or update entire user document
	 */
	public void saveUserData(String userId, Map<String, Object> data) {
		try {
			logger.info("DAO: Saving user data for userId: {}", userId);
			// Ensure user_id is set
			data.put("user_id", userId);
            // NOTE: If the data map does not contain an _id field, mongoTemplate.save will perform an insert. 
            // If it does contain an _id, it performs an update/upsert.
			mongoTemplate.save(data, COLLECTION_NAME); 
			logger.info("DAO: Successfully saved user data for userId: {}", userId);
		} catch (Exception e) {
			logger.error("DAO: Error saving user data for userId: {} - {}", userId, e.getMessage(), e);
			throw new RuntimeException("Failed to save user data", e);
		}
	}

	/**
	 * Update specific field in user document
	 */
	public void updateField(String userId, String field, Object value) {
		try {
			logger.info("DAO: Updating field '{}' for userId: {}", field, userId);
			Query query = new Query(Criteria.where("user_id").is(userId));
			Update update = new Update().set(field, value);

			// Use upsert: create if doesn't exist, update if exists
			mongoTemplate.upsert(query, update, Map.class, COLLECTION_NAME);
			logger.info("DAO: Successfully updated field '{}' for userId: {}", field, userId);
		} catch (Exception e) {
			logger.error("DAO: Error updating field '{}' for userId: {} - {}", field, userId, e.getMessage(), e);
			throw new RuntimeException("Failed to update field", e);
		}
	}

	/**
	 * Delete user document
	 */
	public void deleteUserData(String userId) {
		try {
			logger.info("DAO: Deleting user data for userId: {}", userId);
			Query query = new Query(Criteria.where("user_id").is(userId));
			mongoTemplate.remove(query, COLLECTION_NAME);
			logger.info("DAO: Successfully deleted user data for userId: {}", userId);
		} catch (Exception e) {
			logger.error("DAO: Error deleting user data for userId: {} - {}", userId, e.getMessage(), e);
			throw new RuntimeException("Failed to delete user data", e);
		}
	}

	// ============ SECTION-SPECIFIC OPERATIONS ============

	/**
	 * Get vehicles for a user
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getUserVehicles(String userId) {
		try {
			Map<String, Object> userData = findUserData(userId);
			if (userData != null && userData.containsKey("vehicles")) {
				return (List<Map<String, Object>>) userData.get("vehicles");
			}
			return new ArrayList<>();
		} catch (Exception e) {
			logger.error("DAO: Error getting vehicles for userId: {} - {}", userId, e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	/**
	 * Add a vehicle to user's vehicles list
	 */
	public void addVehicle(String userId, Map<String, Object> vehicle) {
		try {
			Map<String, Object> userData = findUserData(userId);
			if (userData == null) {
				// Create new user document
				userData = createInitialUserData(userId);
			}

			List<Map<String, Object>> vehicles = getOrCreateList(userData, "vehicles");

			// Check if vehicle with same ID already exists
			String vehicleId = (String) vehicle.get("vehicle_id");
			// Use array remove/add instead of $push to ensure uniqueness and replacement
			vehicles.removeIf(v -> vehicleId != null && vehicleId.equals(v.get("vehicle_id"))); 

			// Add the new vehicle
			vehicles.add(vehicle);

			// Save updated document
			saveUserData(userId, userData);
			logger.info("DAO: Successfully added vehicle {} for userId: {}", vehicleId, userId);
		} catch (Exception e) {
			logger.error("DAO: Error adding vehicle for userId: {} - {}", userId, e.getMessage(), e);
			throw new RuntimeException("Failed to add vehicle", e);
		}
	}

	/**
	 * Update a specific vehicle
	 */
	public void updateVehicle(String userId, String vehicleId, Map<String, Object> updatedVehicle) {
		try {
			Map<String, Object> userData = findUserData(userId);
			if (userData == null) {
				logger.warn("DAO: User not found for userId: {}", userId);
				return;
			}

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> vehicles = (List<Map<String, Object>>) userData.get("vehicles");
			if (vehicles == null) {
				logger.warn("DAO: No vehicles found for userId: {}", userId);
				return;
			}

			boolean found = false;
			for (int i = 0; i < vehicles.size(); i++) {
				Map<String, Object> vehicle = vehicles.get(i);
				if (vehicleId.equals(vehicle.get("vehicle_id"))) {
					// Preserve the ID and merge updates
					updatedVehicle.put("vehicle_id", vehicleId);
					vehicles.set(i, updatedVehicle);
					found = true;
					break;
				}
			}

			if (found) {
				saveUserData(userId, userData);
				logger.info("DAO: Successfully updated vehicle {} for userId: {}", vehicleId, userId);
			} else {
				logger.warn("DAO: Vehicle {} not found for userId: {}", vehicleId, userId);
			}
		} catch (Exception e) {
			logger.error("DAO: Error updating vehicle {} for userId: {} - {}", vehicleId, userId, e.getMessage(), e);
			throw new RuntimeException("Failed to update vehicle", e);
		}
	}

	/**
	 * Delete a specific vehicle
	 */
	public void deleteVehicle(String userId, String vehicleId) {
		try {
			Map<String, Object> userData = findUserData(userId);
			if (userData == null) {
				logger.warn("DAO: User not found for userId: {}", userId);
				return;
			}

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> vehicles = (List<Map<String, Object>>) userData.get("vehicles");
			if (vehicles == null) {
				logger.warn("DAO: No vehicles found for userId: {}", userId);
				return;
			}

			boolean removed = vehicles.removeIf(vehicle -> vehicleId.equals(vehicle.get("vehicle_id")));

			if (removed) {
				saveUserData(userId, userData);
				logger.info("DAO: Successfully deleted vehicle {} for userId: {}", vehicleId, userId);
			} else {
				logger.warn("DAO: Vehicle {} not found for userId: {}", vehicleId, userId);
			}
		} catch (Exception e) {
			logger.error("DAO: Error deleting vehicle {} for userId: {} - {}", vehicleId, userId, e.getMessage(), e);
			throw new RuntimeException("Failed to delete vehicle", e);
		}
	}
    
    // ... (All other methods remain the same)

	// ============ SIMILAR METHODS FOR OTHER SECTIONS (Unchanged) ============

	// ... (getUserFavoriteSpots, addFavoriteSpot, getUserActiveStatus, getUserHistory)

	// ============ HELPER METHODS ============

	/**
	 * Create initial user data structure
	 */
	private Map<String, Object> createInitialUserData(String userId) {
		Map<String, Object> userData = new HashMap<>();
		userData.put("user_id", userId);
		userData.put("vehicles", new ArrayList<Map<String, Object>>());
		userData.put("favoriteSpots", new ArrayList<Map<String, Object>>());
		userData.put("activeStatus", new ArrayList<Map<String, Object>>());
		userData.put("history", new ArrayList<Map<String, Object>>());
		userData.put("created_at", java.time.LocalDateTime.now().toString());
		userData.put("updated_at", java.time.LocalDateTime.now().toString());
		return userData;
	}

	/**
	 * Get or create a list from user data
	 */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getOrCreateList(Map<String, Object> userData, String key) {
		List<Map<String, Object>> list = (List<Map<String, Object>>) userData.get(key);
		if (list == null) {
			list = new ArrayList<>();
			userData.put(key, list);
		}
		return list;
	}

	/**
	 * Test MongoDB connection
	 */
	public String testConnection() {
		try {
			String dbName = mongoTemplate.getDb().getName();
			logger.info("DAO: MongoDB connected to database: {}", dbName);
			return "✅ MongoDB Connected! Database: " + dbName;
		} catch (Exception e) {
			logger.error("DAO: MongoDB connection failed: {}", e.getMessage(), e);
			return "❌ MongoDB Connection Failed: " + e.getMessage();
		}
	}

	/**
	 * Get collection statistics
	 */
	public Map<String, Object> getCollectionStats() {
		Map<String, Object> stats = new HashMap<>();
		try {
			// NOTE: countDocuments() is the modern and recommended way to get a document count
			long count = mongoTemplate.getCollection(COLLECTION_NAME).countDocuments(); 
			stats.put("collectionName", COLLECTION_NAME);
			stats.put("documentCount", count);
			stats.put("status", "connected");
			logger.info("DAO: Collection stats - {} documents in {}", count, COLLECTION_NAME);
		} catch (Exception e) {
			stats.put("status", "error");
			stats.put("error", e.getMessage());
			logger.error("DAO: Error getting collection stats: {}", e.getMessage(), e);
		}
		return stats;
	}
}