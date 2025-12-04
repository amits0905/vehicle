package com.park_karo.vehicle.manage;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "manage_data")
public class ManageData {

    private final String userId;

    // Sections to store dynamic data: vehicles, favoriteSpots, history, activeStatus
    private final Map<String, Map<String, Object>> vehicles = new HashMap<>();
    private final Map<String, Map<String, Object>> favoriteSpots = new HashMap<>();
    private final Map<String, Map<String, Object>> history = new HashMap<>();
    private final Map<String, Map<String, Object>> activeStatus = new HashMap<>();

    public ManageData(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    // 🛑 CRITICAL GETTERS FOR JACKSON SERIALIZATION 🛑
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
    // ----------------------------------------------

    private Map<String, Map<String, Object>> getSection(String section) {
        return switch (section) {
            case "vehicles" ->
                vehicles;
            case "favoriteSpots" ->
                favoriteSpots;
            case "history" ->
                history;
            case "activeStatus" ->
                activeStatus;
            default ->
                throw new IllegalArgumentException("Unknown section: " + section);
        };
    }

    public void addToSection(String userId, String section, Map<String, Object> item) {
        Map<String, Map<String, Object>> targetSection = getSection(section);
        String itemId = item.get("id").toString(); // id must be present in item
        targetSection.put(itemId, item);
    }

    public void updateInSection(String userId, String section, String itemId, Map<String, Object> updatedItem) {
        Map<String, Map<String, Object>> targetSection = getSection(section);
        if (targetSection.containsKey(itemId)) {
            targetSection.put(itemId, updatedItem);
        }
    }

    public void deleteFromSection(String userId, String section, String itemId) {
        Map<String, Map<String, Object>> targetSection = getSection(section);
        targetSection.remove(itemId);
    }
}
