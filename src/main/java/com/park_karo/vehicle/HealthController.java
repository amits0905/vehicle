package com.park_karo.vehicle;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

	private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

	@GetMapping("/welcome") // ✅ CHANGED FROM "/" TO "/welcome"
	public String home() {
		logger.info("HealthController.home() called - root endpoint");
		return "Vehicle Service is running! ✅";
	}

	@GetMapping("/health")
	public Map<String, String> health() {
		logger.info("HealthController.health() called");
		Map<String, String> response = new HashMap<>();
		response.put("status", "UP");
		response.put("service", "vehicle-service");
		response.put("version", "1.0.0");
		response.put("timestamp", java.time.Instant.now().toString());
		return response;
	}

	@GetMapping("/ping")
	public String ping() {
		logger.debug("HealthController.ping() called");
		return "pong 🏓";
	}

	@GetMapping("/info")
	public Map<String, String> info() {
		logger.info("HealthController.info() called");
		Map<String, String> info = new HashMap<>();
		info.put("application", "Vehicle Management System");
		info.put("description", "Spring Boot + MongoDB application");
		info.put("status", "Operational");
		return info;
	}

	@GetMapping("/debug/controllers")
	public Map<String, Object> listControllers() {
		logger.info("Listing controllers for debugging");
		Map<String, Object> debugInfo = new HashMap<>();
		debugInfo.put("controller", "HealthController");
		debugInfo.put("methods", new String[] { "home()", "health()", "ping()", "info()" });
		debugInfo.put("paths", new String[] { "/welcome", "/health", "/ping", "/info" }); // ✅ Updated path
		debugInfo.put("timestamp", java.time.Instant.now().toString());
		return debugInfo;
	}
}