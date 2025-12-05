package com.park_karo.vehicle;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

	private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
	private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

	@Autowired(required = false)
	private MongoTemplate mongoTemplate;

	@Autowired
	private ApplicationContext applicationContext;

	// ===========================================
	// PUBLIC ACCESS ENDPOINTS
	// ===========================================

	@GetMapping("/")
	public String root() {
		return "🚀 ParkKaro Vehicle Management Service is running!";
	}

	@GetMapping("/welcome")
	public String welcome() {
		return "Vehicle Service is running! ✅";
	}

	@GetMapping("/ping")
	public Map<String, Object> ping() {
		Map<String, Object> response = new HashMap<>();
		response.put("status", "pong");
		response.put("timestamp", Instant.now().toString());
		response.put("service", "vehicle-service");
		response.put("uptime", getFormattedUptime());
		return response;
	}

	@GetMapping("/info")
	public Map<String, Object> info() {
		Map<String, Object> info = new HashMap<>();
		info.put("application", "ParkKaro Vehicle Management System");
		info.put("description", "Spring Boot + MongoDB application for parking spot management");
		info.put("version", "1.0.0");
		info.put("status", "Operational");
		info.put("timestamp", Instant.now().toString());
		info.put("environment", getActiveProfiles());
		return info;
	}

	// ===========================================
	// HEALTH CHECK ENDPOINTS
	// ===========================================

	@GetMapping("/health")
	public Map<String, Object> basicHealth() {
		Map<String, Object> health = new HashMap<>();
		health.put("service", "vehicle-service");
		health.put("status", "UP");
		health.put("version", "1.0.0");
		health.put("timestamp", Instant.now().toString());
		health.put("uptime", getFormattedUptime());
		return health;
	}

	@GetMapping("/health/check")
	public ResponseEntity<Map<String, Object>> healthCheck() {
		Map<String, Object> healthReport = new HashMap<>();
		healthReport.put("timestamp", LocalDateTime.now().format(formatter));
		healthReport.put("service", "vehicle-service");

		boolean allHealthy = true;
		List<String> unhealthyComponents = new ArrayList<>();
		Map<String, Object> components = new HashMap<>();

		// 1. Check MongoDB
		Map<String, Object> mongoStatus = checkMongoDBHealth();
		components.put("mongodb", mongoStatus);
		if (!"UP".equals(mongoStatus.get("status"))) {
			allHealthy = false;
			unhealthyComponents.add("mongodb");
		}

		// 2. Check Application Context
		Map<String, Object> appStatus = checkApplicationHealth();
		components.put("application", appStatus);
		if (!"UP".equals(appStatus.get("status"))) {
			allHealthy = false;
			unhealthyComponents.add("application");
		}

		// 3. Check Memory Health
		Map<String, Object> memoryStatus = checkMemoryHealth();
		components.put("memory", memoryStatus);
		if (!"HEALTHY".equals(memoryStatus.get("status"))) {
			unhealthyComponents.add("memory");
		}

		healthReport.put("components", components);
		healthReport.put("overallStatus", allHealthy ? "UP" : "DOWN");

		if (!unhealthyComponents.isEmpty()) {
			healthReport.put("unhealthyComponents", unhealthyComponents);
		}

		HttpStatus httpStatus = allHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;

		logger.info("Health check completed: {} - Unhealthy components: {}", allHealthy ? "HEALTHY" : "UNHEALTHY",
				unhealthyComponents);

		return ResponseEntity.status(httpStatus).body(healthReport);
	}

	@GetMapping("/health/detailed")
	public Map<String, Object> detailedDiagnostics() {
		Map<String, Object> diagnostics = new HashMap<>();
		diagnostics.put("timestamp", Instant.now().toString());
		diagnostics.put("requestId", UUID.randomUUID().toString());

		diagnostics.put("system", collectSystemInfo());
		diagnostics.put("application", collectApplicationInfo());
		diagnostics.put("database", collectDatabaseInfo());
		diagnostics.put("memory", collectMemoryInfo());
		diagnostics.put("performance", collectPerformanceMetrics());
		diagnostics.put("dependencies", collectDependencyInfo());

		logger.debug("Detailed diagnostics generated");
		return diagnostics;
	}

	// ===========================================
	// TESTING & DEBUGGING ENDPOINTS
	// ===========================================

	@GetMapping("/test/connection")
	public Map<String, Object> testConnection() {
		Map<String, Object> result = new HashMap<>();
		result.put("test", "MongoDB Connection Test");
		result.put("timestamp", Instant.now().toString());

		if (mongoTemplate == null) {
			result.put("status", "FAILED");
			result.put("error", "MongoTemplate is not initialized");
			result.put("suggestion", "Check MongoDB configuration in application.properties");
			return result;
		}

		try {
			String dbName = mongoTemplate.getDb().getName();
			result.put("database", dbName);

			mongoTemplate.executeCommand("{ ping: 1 }");
			result.put("ping", "SUCCESS");

			// Test server status
			mongoTemplate.executeCommand("{ serverStatus: 1 }");
			result.put("serverStatus", "RETRIEVED");

			List<String> collections = new ArrayList<>();
			mongoTemplate.getDb().listCollectionNames().forEach(collections::add);
			result.put("collections", collections);
			result.put("collectionCount", collections.size());

			result.put("status", "SUCCESS");
			result.put("message", "MongoDB connection is healthy");

			logger.info("Connection test successful for database: {}", dbName);

		} catch (Exception e) {
			result.put("status", "FAILED");
			result.put("error", e.getMessage());
			result.put("errorType", e.getClass().getSimpleName());

			logger.error("Connection test failed: {}", e.getMessage(), e);
		}

		return result;
	}

	@GetMapping("/check/mongo")
	public Map<String, Object> checkMongoConfiguration() {
		Map<String, Object> config = new HashMap<>();
		config.put("check", "MongoDB Configuration");
		config.put("timestamp", Instant.now().toString());

		try {
			boolean hasMongoTemplate = applicationContext.getBeanNamesForType(MongoTemplate.class).length > 0;
			config.put("hasMongoTemplateBean", hasMongoTemplate);

			boolean hasMongoClient = applicationContext
					.getBeanNamesForType(com.mongodb.client.MongoClient.class).length > 0;
			config.put("hasMongoClientBean", hasMongoClient);

			if (hasMongoTemplate && mongoTemplate != null) {
				try {
					String dbName = mongoTemplate.getDb().getName();
					config.put("database", dbName);
					config.put("connectionString", "Configured (hidden for security)");
					config.put("status", "CONFIGURED");
					config.put("message", "MongoDB is properly configured");
				} catch (Exception e) {
					config.put("status", "CONFIGURED_BUT_ERROR");
					config.put("error", e.getMessage());
				}
			} else {
				config.put("status", "NOT_CONFIGURED");
				config.put("message", "MongoDB is not configured in Spring context");
			}

		} catch (Exception e) {
			config.put("status", "ERROR");
			config.put("error", e.getMessage());
		}

		return config;
	}

	@GetMapping("/system/info")
	public Map<String, Object> systemInformation() {
		return collectSystemInfo();
	}

	@GetMapping("/app/info")
	public Map<String, Object> applicationInformation() {
		return collectApplicationInfo();
	}

	@GetMapping("/db/info")
	public Map<String, Object> databaseInformation() {
		return collectDatabaseInfo();
	}

	@GetMapping("/memory/info")
	public Map<String, Object> memoryInformation() {
		return collectMemoryInfo();
	}

	// ===========================================
	// PRIVATE HEALTH CHECK METHODS
	// ===========================================

	private Map<String, Object> checkMongoDBHealth() {
		Map<String, Object> mongoHealth = new HashMap<>();
		mongoHealth.put("component", "MongoDB");
		mongoHealth.put("type", "database");

		if (mongoTemplate == null) {
			mongoHealth.put("status", "DOWN");
			mongoHealth.put("error", "MongoTemplate not initialized");
			mongoHealth.put("severity", "CRITICAL");
			return mongoHealth;
		}

		try {
			String dbName = mongoTemplate.getDb().getName();
			mongoHealth.put("database", dbName);

			mongoTemplate.executeCommand("{ ping: 1 }");
			mongoHealth.put("ping", "OK");

			int collectionCount = 0;
			List<String> collections = new ArrayList<>();
			for (String collection : mongoTemplate.getDb().listCollectionNames()) {
				collections.add(collection);
				collectionCount++;
			}
			mongoHealth.put("collectionCount", collectionCount);
			mongoHealth.put("collections", collections);

			mongoHealth.put("status", "UP");
			mongoHealth.put("message", "MongoDB is connected and responsive");

		} catch (Exception e) {
			mongoHealth.put("status", "DOWN");
			mongoHealth.put("error", e.getMessage());
			mongoHealth.put("severity", "CRITICAL");
			mongoHealth.put("message", "Failed to connect to MongoDB");
		}

		return mongoHealth;
	}

	private Map<String, Object> checkApplicationHealth() {
		Map<String, Object> appHealth = new HashMap<>();
		appHealth.put("component", "Application");
		appHealth.put("type", "service");

		try {
			int beanCount = applicationContext.getBeanDefinitionCount();
			appHealth.put("beanCount", beanCount);

			String[] profiles = applicationContext.getEnvironment().getActiveProfiles();
			appHealth.put("activeProfiles", profiles.length > 0 ? profiles : new String[] { "default" });

			appHealth.put("springBootVersion", SpringBootVersion.getVersion());
			appHealth.put("uptime", getFormattedUptime());
			appHealth.put("startTime", Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime()));

			appHealth.put("status", "UP");
			appHealth.put("message", "Application context is healthy");

		} catch (Exception e) {
			appHealth.put("status", "DOWN");
			appHealth.put("error", e.getMessage());
			appHealth.put("severity", "CRITICAL");
		}

		return appHealth;
	}

	private Map<String, Object> checkMemoryHealth() {
		Map<String, Object> memoryHealth = new HashMap<>();
		memoryHealth.put("component", "Memory");
		memoryHealth.put("type", "resource");

		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory();
		long totalMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		long usedMemory = totalMemory - freeMemory;

		double usagePercentage = (usedMemory * 100.0) / maxMemory;

		memoryHealth.put("max", formatBytes(maxMemory));
		memoryHealth.put("total", formatBytes(totalMemory));
		memoryHealth.put("free", formatBytes(freeMemory));
		memoryHealth.put("used", formatBytes(usedMemory));
		memoryHealth.put("usagePercentage", String.format("%.2f%%", usagePercentage));

		if (usagePercentage > 90) {
			memoryHealth.put("status", "CRITICAL");
			memoryHealth.put("message", "Memory usage is very high");
		} else if (usagePercentage > 80) {
			memoryHealth.put("status", "WARNING");
			memoryHealth.put("message", "Memory usage is high");
		} else {
			memoryHealth.put("status", "HEALTHY");
			memoryHealth.put("message", "Memory usage is normal");
		}

		return memoryHealth;
	}

	// ===========================================
	// PRIVATE COLLECTION METHODS
	// ===========================================

	private Map<String, Object> collectSystemInfo() {
		Map<String, Object> system = new HashMap<>();

		system.put("javaVersion", System.getProperty("java.version"));
		system.put("javaVendor", System.getProperty("java.vendor"));
		system.put("javaHome", System.getProperty("java.home"));
		system.put("javaVmVersion", System.getProperty("java.vm.version"));

		system.put("osName", System.getProperty("os.name"));
		system.put("osVersion", System.getProperty("os.version"));
		system.put("osArch", System.getProperty("os.arch"));
		system.put("userName", System.getProperty("user.name"));
		system.put("userHome", System.getProperty("user.home"));

		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		system.put("jvmName", runtimeMxBean.getVmName());
		system.put("jvmVendor", runtimeMxBean.getVmVendor());
		system.put("jvmVersion", runtimeMxBean.getVmVersion());
		system.put("jvmUptime", runtimeMxBean.getUptime());
		system.put("jvmStartTime", Instant.ofEpochMilli(runtimeMxBean.getStartTime()));

		system.put("availableProcessors", Runtime.getRuntime().availableProcessors());

		return system;
	}

	private Map<String, Object> collectApplicationInfo() {
		Map<String, Object> app = new HashMap<>();

		app.put("name", "ParkKaro Vehicle Management Service");
		app.put("version", "1.0.0");
		app.put("description", "Manage vehicles, parking spots, and user data");
		app.put("springBootVersion", SpringBootVersion.getVersion());

		String[] profiles = applicationContext.getEnvironment().getActiveProfiles();
		app.put("activeProfiles", profiles.length > 0 ? profiles : new String[] { "default" });

		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		app.put("startTime", Instant.ofEpochMilli(runtimeMxBean.getStartTime()));
		app.put("uptime", runtimeMxBean.getUptime());
		app.put("uptimeFormatted", getFormattedUptime());

		app.put("beanDefinitionCount", applicationContext.getBeanDefinitionCount());

		return app;
	}

	private Map<String, Object> collectDatabaseInfo() {
		Map<String, Object> db = new HashMap<>();

		if (mongoTemplate == null) {
			db.put("status", "NOT_CONFIGURED");
			db.put("message", "MongoDB is not configured");
			return db;
		}

		try {
			String dbName = mongoTemplate.getDb().getName();
			db.put("status", "CONNECTED");
			db.put("database", dbName);
			db.put("type", "MongoDB Atlas");

			List<Map<String, Object>> collections = new ArrayList<>();
			int totalDocuments = 0;

			for (String collectionName : mongoTemplate.getDb().listCollectionNames()) {
				Map<String, Object> collectionInfo = new HashMap<>();
				collectionInfo.put("name", collectionName);

				try {
					long documentCount = mongoTemplate.getCollection(collectionName).countDocuments();
					collectionInfo.put("documentCount", documentCount);
					totalDocuments += documentCount;
				} catch (Exception e) {
					collectionInfo.put("documentCount", "UNAVAILABLE");
				}

				collections.add(collectionInfo);
			}

			db.put("collections", collections);
			db.put("collectionCount", collections.size());
			db.put("totalDocuments", totalDocuments);

		} catch (Exception e) {
			db.put("status", "ERROR");
			db.put("error", e.getMessage());
			db.put("message", "Failed to retrieve database information");
		}

		return db;
	}

	private Map<String, Object> collectMemoryInfo() {
		Map<String, Object> memory = new HashMap<>();

		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory();
		long totalMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		long usedMemory = totalMemory - freeMemory;

		double usagePercentage = (usedMemory * 100.0) / maxMemory;

		memory.put("max", formatBytes(maxMemory));
		memory.put("total", formatBytes(totalMemory));
		memory.put("free", formatBytes(freeMemory));
		memory.put("used", formatBytes(usedMemory));
		memory.put("usagePercentage", usagePercentage);
		memory.put("usagePercentageFormatted", String.format("%.2f%%", usagePercentage));

		memory.put("warningThreshold", "80%");
		memory.put("criticalThreshold", "90%");

		return memory;
	}

	private Map<String, Object> collectPerformanceMetrics() {
		Map<String, Object> metrics = new HashMap<>();

		metrics.put("threadCount", Thread.activeCount());

		try {
			List<Map<String, Object>> memoryPools = new ArrayList<>();
			for (java.lang.management.MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
				Map<String, Object> poolInfo = new HashMap<>();
				poolInfo.put("name", pool.getName());
				poolInfo.put("type", pool.getType().toString());
				poolInfo.put("usage", pool.getUsage().getUsed());
				poolInfo.put("usageFormatted", formatBytes(pool.getUsage().getUsed()));
				memoryPools.add(poolInfo);
			}
			metrics.put("memoryPools", memoryPools);
		} catch (Exception e) {
			// Ignore - not critical
		}

		return metrics;
	}

	private Map<String, Object> collectDependencyInfo() {
		Map<String, Object> deps = new HashMap<>();

		deps.put("springBoot", SpringBootVersion.getVersion());
		deps.put("mongodb", "Atlas (Cloud)");

		Map<String, String> dependencyChecks = new HashMap<>();

		checkDependency("Spring Data MongoDB", "org.springframework.data.mongodb.core.MongoTemplate", dependencyChecks);
		checkDependency("Spring Web", "org.springframework.web.bind.annotation.RestController", dependencyChecks);
		checkDependency("Jackson JSON", "com.fasterxml.jackson.databind.ObjectMapper", dependencyChecks);
		checkDependency("SLF4J", "org.slf4j.Logger", dependencyChecks);

		deps.put("availableDependencies", dependencyChecks);

		return deps;
	}

	private void checkDependency(String name, String className, Map<String, String> results) {
		try {
			Class.forName(className);
			results.put(name, "AVAILABLE");
		} catch (ClassNotFoundException e) {
			results.put(name, "MISSING");
		}
	}

	// ===========================================
	// UTILITY METHODS
	// ===========================================

	private String getFormattedUptime() {
		try {
			long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
			return formatUptime(uptime);
		} catch (Exception e) {
			return "unknown";
		}
	}

	private String formatUptime(long millis) {
		long seconds = millis / 1000 % 60;
		long minutes = millis / (1000 * 60) % 60;
		long hours = millis / (1000 * 60 * 60) % 24;
		long days = millis / (1000 * 60 * 60 * 24);

		if (days > 0) {
			return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
		} else if (hours > 0) {
			return String.format("%dh %dm %ds", hours, minutes, seconds);
		} else if (minutes > 0) {
			return String.format("%dm %ds", minutes, seconds);
		} else {
			return String.format("%ds", seconds);
		}
	}

	private String formatBytes(long bytes) {
		if (bytes < 1024) {
			return bytes + " B";
		}
		int exp = (int) (Math.log(bytes) / Math.log(1024));
		String pre = "KMGTPE".charAt(exp - 1) + "";
		return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
	}

	private String[] getActiveProfiles() {
		return applicationContext.getEnvironment().getActiveProfiles();
	}
}
