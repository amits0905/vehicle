package com.park_karo.vehicle;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

	@GetMapping("/")
	public String home() {
		return "Vehicle Service is running! ✅";
	}

	@GetMapping("/health")
	public Map<String, String> health() {
		Map<String, String> response = new HashMap<>();
		response.put("status", "UP");
		response.put("service", "vehicle-service");
		response.put("version", "1.0.0");
		response.put("timestamp", java.time.Instant.now().toString());
		return response;
	}

	@GetMapping("/ping")
	public String ping() {
		return "pong 🏓";
	}

	@GetMapping("/info")
	public Map<String, String> info() {
		Map<String, String> info = new HashMap<>();
		info.put("application", "Vehicle Management System");
		info.put("description", "Spring Boot + MongoDB application");
		info.put("status", "Operational");
		return info;
	}
}