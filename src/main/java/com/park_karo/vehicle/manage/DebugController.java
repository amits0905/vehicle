package com.park_karo.vehicle.manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    // ============ EXISTING METHODS ============
    
    @GetMapping("/db-check")
    public String checkDatabase() {
        try {
            String dbName = mongoTemplate.getDb().getName();
            boolean collectionExists = mongoTemplate.collectionExists("manage_data");
            long count = collectionExists ? 
                mongoTemplate.getCollection("manage_data").countDocuments() : 0;
            
            return String.format(
                "✅ Database: %s<br>" +
                "✅ Collection 'manage_data' exists: %s<br>" +
                "📊 Total documents: %d",
                dbName, collectionExists, count
            );
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
    
    @GetMapping("/find-user/{userId}")
    public String findUser(@PathVariable String userId) {
        try {
            @SuppressWarnings("unchecked")
			Map<String, Object> userDoc = mongoTemplate.findOne(
                new Query(Criteria.where("user_id").is(userId)), 
                Map.class, 
                "manage_data"
            );
            
            if (userDoc == null) {
                return "❌ User '" + userId + "' NOT FOUND in MongoDB";
            } else {
                return "✅ User FOUND!<br>" +
                       "Document ID: " + userDoc.get("_id") + "<br>" +
                       "User ID: " + userDoc.get("user_id") + "<br>" +
                       "Has vehicles? " + userDoc.containsKey("vehicles");
            }
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
    
    @SuppressWarnings("rawtypes")
	@GetMapping("/show-all")
    public List<Map> showAllUsers() {
        return mongoTemplate.findAll(Map.class, "manage_data");
    }
    
    @GetMapping("/delete-user/{userId}")
    public String deleteUser(@PathVariable String userId) {
        try {
            mongoTemplate.remove(
                new Query(Criteria.where("user_id").is(userId)),
                "manage_data"
            );
            return "✅ Deleted document for user " + userId;
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
    
    // ============ NEW METHODS ============
    
    @SuppressWarnings("rawtypes")
	@GetMapping("/which-document/{userId}")
    public String whichDocument(@PathVariable String userId) {
        try {
            Query query = new Query(Criteria.where("user_id").is(userId));
            List<Map> allDocs = mongoTemplate.find(query, Map.class, "manage_data");
            
            StringBuilder result = new StringBuilder();
            result.append("<h3>Found ").append(allDocs.size()).append(" documents for user_id = ").append(userId).append("</h3>");
            
            if (allDocs.isEmpty()) {
                result.append("<p>No documents found!</p>");
                return result.toString();
            }
            
            for (int i = 0; i < allDocs.size(); i++) {
                Map doc = allDocs.get(i);
                result.append("<hr>");
                result.append("<h4>Document ").append(i + 1).append(":</h4>");
                result.append("<ul>");
                result.append("<li><strong>_id:</strong> ").append(doc.get("_id")).append("</li>");
                
                Object vehiclesObj = doc.get("vehicles");
                int vehicleCount = 0;
                if (vehiclesObj instanceof List) {
                    vehicleCount = ((List) vehiclesObj).size();
                }
                result.append("<li><strong>vehicles count:</strong> ").append(vehicleCount).append("</li>");
                
                Object spotsObj = doc.get("favoriteSpots");
                int spotCount = 0;
                if (spotsObj instanceof List) {
                    spotCount = ((List) spotsObj).size();
                }
                result.append("<li><strong>favoriteSpots count:</strong> ").append(spotCount).append("</li>");
                
                Object statusObj = doc.get("activeStatus");
                int statusCount = 0;
                if (statusObj instanceof List) {
                    statusCount = ((List) statusObj).size();
                }
                result.append("<li><strong>activeStatus count:</strong> ").append(statusCount).append("</li>");
                
                Object historyObj = doc.get("history");
                int historyCount = 0;
                if (historyObj instanceof List) {
                    historyCount = ((List) historyObj).size();
                }
                result.append("<li><strong>history count:</strong> ").append(historyCount).append("</li>");
                
                result.append("<li><strong>created_at:</strong> ").append(doc.get("created_at")).append("</li>");
                result.append("<li><strong>updated_at:</strong> ").append(doc.get("updated_at")).append("</li>");
                result.append("</ul>");
            }
            
            return result.toString();
        } catch (Exception e) {
            return "<p style='color:red'>Error: " + e.getMessage() + "</p>";
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping("/clean-duplicates/{userId}")
    public String cleanDuplicates(@PathVariable String userId) {
        try {
            Query query = new Query(Criteria.where("user_id").is(userId));
            List<Map> allDocs = mongoTemplate.find(query, Map.class, "manage_data");
            
            if (allDocs.size() <= 1) {
                return "<p>No duplicates found for user " + userId + "</p>";
            }
            
            Map<String, Object> keeper = null;
            List<String> idsToDelete = new ArrayList<>();
            
            for (Map doc : allDocs) {
                boolean hasData = false;
                
                Object vehiclesObj = doc.get("vehicles");
                if (vehiclesObj instanceof List && !((List) vehiclesObj).isEmpty()) {
                    hasData = true;
                }
                
                Object spotsObj = doc.get("favoriteSpots");
                if (!hasData && spotsObj instanceof List && !((List) spotsObj).isEmpty()) {
                    hasData = true;
                }
                
                if (hasData) {
                    keeper = doc;
                } else {
                    idsToDelete.add(doc.get("_id").toString());
                }
            }
            
            int deletedCount = 0;
            for (String id : idsToDelete) {
                Query deleteQuery = new Query(Criteria.where("_id").is(id));
                mongoTemplate.remove(deleteQuery, "manage_data");
                deletedCount++;
            }
            
            return String.format(
                "<h3>Cleaned duplicates!</h3>" +
                "<p><strong>Kept document with _id:</strong> %s</p>" +
                "<p><strong>Deleted %d empty document(s)</strong></p>",
                keeper != null ? keeper.get("_id") : "none",
                deletedCount
            );
        } catch (Exception e) {
            return "<p style='color:red'>Error: " + e.getMessage() + "</p>";
        }
    }
    
    @PostMapping("/insert-sample/{userId}")
    public String insertSample(@PathVariable String userId) {
        try {
            Map<String, Object> sample = new HashMap<>();
            sample.put("_id", userId);
            sample.put("user_id", userId);
            
            List<Map<String, Object>> vehicles = new ArrayList<>();
            vehicles.add(Map.of(
                "vehicle_id", "test-v1",
                "nickname", "Test Car",
                "type", "4-wheeler",
                "icon", "car",
                "is_active", true,
                "created_at", "2025-12-01T08:00:00Z",
                "updated_at", "2025-12-01T08:00:00Z"
            ));
            sample.put("vehicles", vehicles);
            
            List<Map<String, Object>> spots = new ArrayList<>();
            spots.add(Map.of(
                "spot_id", "test-s1",
                "name", "Test Location",
                "location", "123 Test Street",
                "icon", "map-pin",
                "created_at", "2025-11-25T08:00:00Z",
                "updated_at", "2025-11-25T08:00:00Z"
            ));
            sample.put("favoriteSpots", spots);
            
            sample.put("activeStatus", new ArrayList<>());
            sample.put("history", new ArrayList<>());
            sample.put("created_at", "2025-11-25T08:00:00Z");
            sample.put("updated_at", "2025-12-02T07:00:00Z");
            
            mongoTemplate.save(sample, "manage_data");
            
            return "<p style='color:green'>✅ Sample data inserted for user " + userId + "!</p>";
        } catch (Exception e) {
            return "<p style='color:red'>❌ Error: " + e.getMessage() + "</p>";
        }
    }
    
    @PostMapping("/insert-full-sample")
    public String insertFullSample() {
        try {
            String sampleJson = "{"
                + "\"_id\": \"u123\","
                + "\"user_id\": \"u123\","
                + "\"vehicles\": ["
                + "  {\"vehicle_id\": \"v1\", \"nickname\": \"Speedy Bike\", \"type\": \"2-wheeler\", \"icon\": \"motorcycle\", \"is_active\": true, \"created_at\": \"2025-12-01T08:00:00Z\", \"updated_at\": \"2025-12-01T08:00:00Z\"},"
                + "  {\"vehicle_id\": \"v2\", \"nickname\": \"Family Car\", \"type\": \"4-wheeler\", \"icon\": \"car\", \"is_active\": true, \"created_at\": \"2025-12-01T09:00:00Z\", \"updated_at\": \"2025-12-01T09:00:00Z\"},"
                + "  {\"vehicle_id\": \"v3\", \"nickname\": \"Delivery Van\", \"type\": \"4-wheeler\", \"icon\": \"van-shuttle\", \"is_active\": false, \"created_at\": \"2025-11-30T10:30:00Z\", \"updated_at\": \"2025-12-01T10:00:00Z\"},"
                + "  {\"vehicle_id\": \"v4\", \"nickname\": \"Electric Scooter\", \"type\": \"2-wheeler\", \"icon\": \"scooter\", \"is_active\": true, \"created_at\": \"2025-12-02T07:00:00Z\", \"updated_at\": \"2025-12-02T07:00:00Z\"}"
                + "],"
                + "\"favoriteSpots\": ["
                + "  {\"spot_id\": \"s1\", \"name\": \"Home\", \"location\": \"123 Main Street\", \"icon\": \"home\", \"created_at\": \"2025-11-25T08:00:00Z\", \"updated_at\": \"2025-11-25T08:00:00Z\"},"
                + "  {\"spot_id\": \"s2\", \"name\": \"Gym Lot\", \"location\": \"BKC Gym Street\", \"icon\": \"dumbbell\", \"created_at\": \"2025-11-26T08:30:00Z\", \"updated_at\": \"2025-11-26T08:30:00Z\"},"
                + "  {\"spot_id\": \"s3\", \"name\": \"Office Parking\", \"location\": \"BKC Corporate Park\", \"icon\": \"building\", \"created_at\": \"2025-11-27T09:00:00Z\", \"updated_at\": \"2025-11-27T09:00:00Z\"},"
                + "  {\"spot_id\": \"s4\", \"name\": \"Mall Parking\", \"location\": \"BKC Mall Street\", \"icon\": \"shopping-bag\", \"created_at\": \"2025-12-01T10:00:00Z\", \"updated_at\": \"2025-12-01T10:00:00Z\"}"
                + "],"
                + "\"activeStatus\": ["
                + "  {\"active_id\": \"a1\", \"type\": \"parking\", \"title\": \"BKC Lot A\", \"subtitle\": \"Ends in 1h 30m\", \"icon\": \"motorcycle\", \"status\": \"active\", \"expiry_time\": \"2025-12-02T14:30:00Z\", \"created_at\": \"2025-12-01T10:00:00Z\", \"updated_at\": \"2025-12-01T10:00:00Z\"},"
                + "  {\"active_id\": \"a2\", \"type\": \"pass\", \"title\": \"Monthly Pass\", \"subtitle\": \"Valid until 31 Dec\", \"icon\": \"ticket\", \"status\": \"active\", \"expiry_time\": \"2025-12-31T23:59:59Z\", \"created_at\": \"2025-12-01T11:00:00Z\", \"updated_at\": \"2025-12-01T11:00:00Z\"},"
                + "  {\"active_id\": \"a3\", \"type\": \"ticket\", \"title\": \"Weekend Parking\", \"subtitle\": \"Expires 3 Dec\", \"icon\": \"ticket-alt\", \"status\": \"active\", \"expiry_time\": \"2025-12-03T23:59:59Z\", \"created_at\": \"2025-12-01T12:00:00Z\", \"updated_at\": \"2025-12-01T12:00:00Z\"},"
                + "  {\"active_id\": \"a4\", \"type\": \"parking\", \"title\": \"Mall Parking\", \"subtitle\": \"Expires 4 Dec\", \"icon\": \"car\", \"status\": \"active\", \"expiry_time\": \"2025-12-04T20:00:00Z\", \"created_at\": \"2025-12-02T07:00:00Z\", \"updated_at\": \"2025-12-02T07:00:00Z\"}"
                + "],"
                + "\"history\": ["
                + "  {\"history_id\": \"h1\", \"type\": \"parking\", \"title\": \"Parking: Work Lot - 2h - $10\", \"date\": \"2025-11-30T12:00:00Z\", \"status\": \"completed\", \"price\": 10, \"notes\": \"\", \"icon\": \"map-pin\", \"created_at\": \"2025-11-30T12:00:00Z\", \"updated_at\": \"2025-11-30T12:00:00Z\"},"
                + "  {\"history_id\": \"h2\", \"type\": \"ticket\", \"title\": \"Monthly Ticket - Paid\", \"date\": \"2025-11-28T10:00:00Z\", \"status\": \"completed\", \"price\": 50, \"notes\": \"Left early\", \"icon\": \"ticket-alt\", \"created_at\": \"2025-11-28T10:00:00Z\", \"updated_at\": \"2025-11-28T10:00:00Z\"},"
                + "  {\"history_id\": \"h3\", \"type\": \"parking\", \"title\": \"Weekend Parking - 4h - $15\", \"date\": \"2025-11-29T09:00:00Z\", \"status\": \"completed\", \"price\": 15, \"notes\": \"Parked near Gym\", \"icon\": \"map-pin\", \"created_at\": \"2025-11-29T09:00:00Z\", \"updated_at\": \"2025-11-29T09:00:00Z\"},"
                + "  {\"history_id\": \"h4\", \"type\": \"ticket\", \"title\": \"Mall Parking Ticket\", \"date\": \"2025-12-01T14:00:00Z\", \"status\": \"completed\", \"price\": 20, \"notes\": \"Evening shopping\", \"icon\": \"ticket\", \"created_at\": \"2025-12-01T14:00:00Z\", \"updated_at\": \"2025-12-01T14:00:00Z\"}"
                + "],"
                + "\"created_at\": \"2025-11-25T08:00:00Z\","
                + "\"updated_at\": \"2025-12-02T07:00:00Z\""
                + "}";
            
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
			Map<String, Object> sampleData = mapper.readValue(sampleJson, Map.class);
            
            mongoTemplate.save(sampleData, "manage_data");
            
            return "<p style='color:green'>✅ Full sample data inserted!</p>"
                 + "<p>Now test: <a href='/manage/u123'>/manage/u123</a></p>";
        } catch (Exception e) {
            return "<p style='color:red'>❌ Error: " + e.getMessage() + "</p>";
        }
    }
}