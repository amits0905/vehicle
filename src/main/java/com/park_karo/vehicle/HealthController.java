package com.park_karo.vehicle;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
// Import the new file handling classes
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.park_karo.vehicle.exception.CustomExceptions;

import jakarta.annotation.PostConstruct;

@RestController
public class HealthController {

	private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
	private static final String SERVICE_NAME = "ParkKaro Vehicle Management Service";
	private static final String SERVICE_VERSION = "2.0.0";

	// File health check constants
	private static final List<String> REQUIRED_FILES = Arrays.asList("application.properties", "render.yaml",
			"Dockerfile");

	private static final Map<String, String> FILE_DESCRIPTIONS = new HashMap<>();
	static {
		FILE_DESCRIPTIONS.put("application.properties", "Spring Boot application configuration file");
		FILE_DESCRIPTIONS.put("render.yaml", "Render deployment configuration file");
		FILE_DESCRIPTIONS.put("Dockerfile", "Docker container definition file");
	}

	@Autowired(required = false)
	private MongoTemplate mongoTemplate;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private Environment environment;

	// ===========================================
	// INITIALIZATION - VERIFIES CONTROLLER IS LOADED
	// ===========================================

	@PostConstruct
	public void init() {
		List<String> endpoints = Arrays.asList("/ - Root endpoint", "/welcome - Welcome message",
				"/ping - Simple ping with response", "/info - Service information", "/health - Basic health status",
				"/health/check - Comprehensive health check", "/health/detailed - Detailed diagnostics",
				"/health/checks - Individual health checks", "/health/files - Required files health check",
				"/files/status - File system status", "/files/details - Detailed file information",
				"/config/check - Configuration files validation", "/test/connection - MongoDB connection test",
				"/check/mongo - MongoDB configuration check", "/system/info - System information",
				"/app/info - Application information", "/db/info - Database information",
				"/memory/info - Memory information", "/db/status - Database status with validation",
				"/db/diagnostics - Database diagnostics", "/system/status - System resource status",
				"/metrics - Performance metrics");

		logger.info("🔄 HealthController initialized with {} endpoints:", endpoints.size());
		for (String endpoint : endpoints) {
			logger.info("   🔗 {}", endpoint);
		}
		logger.info("📡 Health endpoints available at: http://localhost:8080/");
	}

	// ===========================================
	// PUBLIC ACCESS ENDPOINTS
	// ===========================================

	@GetMapping("/")
	public Map<String, Object> root() {
		Map<String, Object> response = new HashMap<>();
		response.put("service", SERVICE_NAME);
		response.put("version", SERVICE_VERSION);
		response.put("status", "RUNNING");
		response.put("timestamp", Instant.now());
		response.put("uptime", getFormattedUptime());
		response.put("documentation",
				Map.of("health", "http://localhost:8080/health", "database", "http://localhost:8080/db/status",
						"system", "http://localhost:8080/system/info", "all_endpoints",
						"Check logs for full endpoint list"));
		return response;
	}

	@GetMapping("/welcome")
	public String welcome() {
		logger.debug("Welcome endpoint accessed");
		return "🚗 ParkKaro Vehicle Management Service is running! ✅";
	}

	@GetMapping("/ping")
	public ResponseEntity<Map<String, Object>> ping() {
		logger.debug("Ping endpoint accessed");

		Map<String, Object> response = new HashMap<>();
		response.put("service", SERVICE_NAME);
		response.put("status", "PONG");
		response.put("timestamp", Instant.now());
		response.put("uptime", getFormattedUptime());
		response.put("requestId", UUID.randomUUID().toString());

		try {
			// Quick health checks
			response.put("application", Map.of("status", "UP", "beans", applicationContext.getBeanDefinitionCount()));

			if (mongoTemplate != null) {
				try {
					long start = System.currentTimeMillis();
					mongoTemplate.executeCommand("{ ping: 1 }");
					long latency = System.currentTimeMillis() - start;
					response.put("database",
							Map.of("status", "UP", "latencyMs", latency, "database", mongoTemplate.getDb().getName()));
				} catch (Exception e) {
					response.put("database", Map.of("status", "DOWN", "error", e.getMessage()));
				}
			} else {
				response.put("database", Map.of("status", "NOT_CONFIGURED"));
			}

			// Memory check
			Runtime runtime = Runtime.getRuntime();
			long used = runtime.totalMemory() - runtime.freeMemory();
			long max = runtime.maxMemory();
			double percent = (used * 100.0) / max;
			response.put("memory", Map.of("usage", String.format("%.1f%%", percent), "status",
					percent > 90 ? "CRITICAL" : percent > 80 ? "WARNING" : "OK"));

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			logger.error("Ping endpoint failed", e);
			throw new CustomExceptions.DatabaseConnectionBusinessException("Ping health check failed", e);
		}
	}

	@GetMapping("/info")
	public Map<String, Object> info() {
		logger.debug("Info endpoint accessed");

		Map<String, Object> info = new HashMap<>();
		info.put("name", SERVICE_NAME);
		info.put("version", SERVICE_VERSION);
		info.put("description", "Advanced vehicle and parking management system");
		info.put("environment", getActiveProfiles());
		info.put("status", "OPERATIONAL");
		info.put("timestamp", Instant.now());
		info.put("build",
				Map.of("springBootVersion", SpringBootVersion.getVersion(), "javaVersion",
						System.getProperty("java.version"), "startTime",
						Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime()), "uptime",
						getFormattedUptime()));
		info.put("contacts", Map.of("support", "support@parkkaro.com", "documentation", "https://docs.parkkaro.com"));

		return info;
	}

	// ===========================================
	// BASIC HEALTH CHECK ENDPOINTS
	// ===========================================

	@GetMapping("/health")
	public Map<String, Object> basicHealth() {
		logger.debug("Basic health endpoint accessed");

		Map<String, Object> health = new HashMap<>();
		health.put("service", SERVICE_NAME);
		health.put("version", SERVICE_VERSION);
		health.put("status", "UP");
		health.put("timestamp", Instant.now());
		health.put("uptime", getFormattedUptime());

		// Quick status
		health.put("application", "RUNNING");
		health.put("environment", getActiveProfiles());

		return health;
	}

	@GetMapping("/health/check")
	public ResponseEntity<Map<String, Object>> healthCheck() {
		logger.debug("Comprehensive health check endpoint accessed");

		Map<String, Object> healthReport = new HashMap<>();
		healthReport.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
		healthReport.put("service", SERVICE_NAME);
		healthReport.put("requestId", UUID.randomUUID().toString());

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

		// 4. Check Database Collections
		Map<String, Object> collectionsStatus = checkCollectionsHealth();
		components.put("collections", collectionsStatus);
		if (!"HEALTHY".equals(collectionsStatus.get("status"))) {
			unhealthyComponents.add("collections");
		}

		// 5. Check Required Files
		Map<String, Object> filesStatus = checkAllFilesHealth();
		components.put("files", filesStatus);
		if (!"HEALTHY".equals(filesStatus.get("overallStatus"))) {
			unhealthyComponents.add("files");
		}

		healthReport.put("components", components);
		healthReport.put("overallStatus", allHealthy ? "UP" : "DOWN");
		healthReport.put("checksPerformed", components.size());

		if (!unhealthyComponents.isEmpty()) {
			healthReport.put("unhealthyComponents", unhealthyComponents);
			healthReport.put("unhealthyCount", unhealthyComponents.size());
		}

		HttpStatus httpStatus = allHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;

		logger.info("Health check completed: {} - Status: {}", allHealthy ? "HEALTHY" : "UNHEALTHY",
				unhealthyComponents);

		return ResponseEntity.status(httpStatus).body(healthReport);
	}

	// ===========================================
	// FILE HEALTH CHECK ENDPOINTS
	// ===========================================

	@GetMapping("/health/files")
	public ResponseEntity<Map<String, Object>> checkRequiredFiles() {
		logger.debug("Required files health check endpoint accessed");

		Map<String, Object> response = new HashMap<>();
		response.put("timestamp", Instant.now());
		response.put("service", SERVICE_NAME);
		response.put("check", "Required Files Health Check");
		response.put("requestId", UUID.randomUUID().toString());

		Map<String, Object> filesStatus = checkAllFilesHealth();
		response.putAll(filesStatus);

		boolean allFilesHealthy = "HEALTHY".equals(filesStatus.get("overallStatus"));
		HttpStatus httpStatus = allFilesHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;

		logger.info("File health check completed: {}", allFilesHealthy ? "HEALTHY" : "UNHEALTHY");

		return ResponseEntity.status(httpStatus).body(response);
	}

	@GetMapping("/files/status")
	public Map<String, Object> getFileStatus() {
		logger.debug("File status endpoint accessed");

		Map<String, Object> response = new HashMap<>();
		response.put("timestamp", Instant.now());
		response.put("service", SERVICE_NAME);
		response.put("requestId", UUID.randomUUID().toString());

		Map<String, Object> files = new HashMap<>();
		int existingCount = 0;
		int missingCount = 0;
		long totalSize = 0;

		for (String fileName : REQUIRED_FILES) {
			Map<String, Object> fileInfo = getFileInfo(fileName);
			files.put(fileName, fileInfo);

			if ("PRESENT".equals(fileInfo.get("status"))) {
				existingCount++;
				if (fileInfo.containsKey("size")) {
					totalSize += (Long) fileInfo.get("size");
				}
			} else {
				missingCount++;
			}
		}

		response.put("files", files);
		response.put("statistics", Map.of("totalRequired", REQUIRED_FILES.size(), "existing", existingCount, "missing",
				missingCount, "totalSizeBytes", totalSize, "totalSizeFormatted", formatBytes(totalSize)));

		response.put("status", missingCount == 0 ? "COMPLETE" : "INCOMPLETE");
		response.put("message", missingCount == 0 ? "All required files are present"
				: String.format("Missing %d required file(s)", missingCount));

		return response;
	}

	@GetMapping("/files/details")
	public Map<String, Object> getFileDetails() {
		logger.debug("File details endpoint accessed");

		Map<String, Object> response = new HashMap<>();
		response.put("timestamp", Instant.now());
		response.put("service", SERVICE_NAME);
		response.put("requestId", UUID.randomUUID().toString());

		Map<String, Object> detailedInfo = new HashMap<>();

		for (String fileName : REQUIRED_FILES) {
			Map<String, Object> details = getDetailedFileInfo(fileName);
			detailedInfo.put(fileName, details);
		}

		response.put("files", detailedInfo);
		response.put("fileDescriptions", FILE_DESCRIPTIONS);
		response.put("check", "File System Health Diagnostics");

		return response;
	}

	@GetMapping("/config/check")
	public Map<String, Object> checkConfigurationFiles() {
		logger.debug("Configuration files check endpoint accessed");

		Map<String, Object> response = new HashMap<>();
		response.put("timestamp", Instant.now());
		response.put("service", SERVICE_NAME);
		response.put("check", "Configuration Files Validation");
		response.put("requestId", UUID.randomUUID().toString());

		Map<String, Object> configChecks = new HashMap<>();
		List<String> warnings = new ArrayList<>();
		List<String> errors = new ArrayList<>();

		// Check application.properties
		Map<String, Object> appPropsCheck = checkApplicationProperties();
		configChecks.put("application.properties", appPropsCheck);
		if ("WARNING".equals(appPropsCheck.get("severity"))) {
			warnings.add("application.properties - " + appPropsCheck.get("message"));
		} else if ("ERROR".equals(appPropsCheck.get("severity"))) {
			errors.add("application.properties - " + appPropsCheck.get("message"));
		}

		// Check render.yaml
		Map<String, Object> renderYamlCheck = checkRenderYaml();
		configChecks.put("render.yaml", renderYamlCheck);
		if ("WARNING".equals(renderYamlCheck.get("severity"))) {
			warnings.add("render.yaml - " + renderYamlCheck.get("message"));
		} else if ("ERROR".equals(renderYamlCheck.get("severity"))) {
			errors.add("render.yaml - " + renderYamlCheck.get("message"));
		}

		// Check Dockerfile
		Map<String, Object> dockerfileCheck = checkDockerfile();
		configChecks.put("Dockerfile", dockerfileCheck);
		if ("WARNING".equals(dockerfileCheck.get("severity"))) {
			warnings.add("Dockerfile - " + dockerfileCheck.get("message"));
		} else if ("ERROR".equals(dockerfileCheck.get("severity"))) {
			errors.add("Dockerfile - " + dockerfileCheck.get("message"));
		}

		response.put("checks", configChecks);
		response.put("warnings", warnings);
		response.put("errors", errors);
		response.put("summary", Map.of("totalChecks", 3, "warnings", warnings.size(), "errors", errors.size(), "status",
				errors.isEmpty() ? (warnings.isEmpty() ? "PASS" : "WARNING") : "FAIL"));

		return response;
	}

	// ===========================================
	// ADVANCED DIAGNOSTICS ENDPOINTS
	// ===========================================

	@GetMapping("/health/detailed")
	public Map<String, Object> detailedDiagnostics() {
		logger.debug("Detailed diagnostics endpoint accessed");

		Map<String, Object> diagnostics = new HashMap<>();
		diagnostics.put("timestamp", Instant.now());
		diagnostics.put("requestId", UUID.randomUUID().toString());
		diagnostics.put("service", SERVICE_NAME);

		diagnostics.put("system", collectSystemInfo());
		diagnostics.put("application", collectApplicationInfo());
		diagnostics.put("database", collectDatabaseInfo());
		diagnostics.put("memory", collectMemoryInfo());
		diagnostics.put("performance", collectPerformanceMetrics());
		diagnostics.put("dependencies", collectDependencyInfo());
		diagnostics.put("environment", collectEnvironmentInfo());
		diagnostics.put("files", checkAllFilesHealth());

		logger.debug("Detailed diagnostics generated successfully");
		return diagnostics;
	}

	@GetMapping("/health/checks")
	public Map<String, Object> individualHealthChecks() {
		logger.debug("Individual health checks endpoint accessed");

		Map<String, Object> checks = new HashMap<>();
		checks.put("timestamp", Instant.now());
		checks.put("service", SERVICE_NAME);
		checks.put("requestId", UUID.randomUUID().toString());

		Map<String, Object> individualChecks = new HashMap<>();

		try {
			individualChecks.put("database", performDatabaseHealthCheck());
			individualChecks.put("memory", performMemoryHealthCheck());
			individualChecks.put("application", performApplicationHealthCheck());
			individualChecks.put("collections", performCollectionsHealthCheck());
			individualChecks.put("connectivity", performConnectivityHealthCheck());
			individualChecks.put("files", checkAllFilesHealth());

			checks.put("checks", individualChecks);
			checks.put("totalChecks", individualChecks.size());
			checks.put("status", "COMPLETE");

		} catch (CustomExceptions.DatabaseConnectionBusinessException e) {
			individualChecks.put("database", Map.of("status", "ERROR", "message", e.getMessage()));
			checks.put("error", "Some health checks failed");
			checks.put("status", "PARTIAL");
			logger.error("Health checks partially failed", e);
		}

		return checks;
	}

	// ===========================================
	// DATABASE STATUS ENDPOINTS
	// ===========================================

	@GetMapping("/db/status")
	public ResponseEntity<Map<String, Object>> databaseStatus() {
		logger.debug("Database status endpoint accessed");

		if (mongoTemplate == null) {
			throw new CustomExceptions.ResourceNotFoundBusinessException("MongoDB Configuration",
					"MongoTemplate is not available. Check your database configuration.");
		}

		try {
			Map<String, Object> status = new HashMap<>();
			status.put("timestamp", Instant.now());
			status.put("database", mongoTemplate.getDb().getName());
			status.put("type", "MongoDB");
			status.put("requestId", UUID.randomUUID().toString());

			// Test connection with timeout
			long startTime = System.currentTimeMillis();
			mongoTemplate.executeCommand("{ ping: 1 }");
			long responseTime = System.currentTimeMillis() - startTime;

			status.put("ping", Map.of("status", "SUCCESS", "responseTimeMs", responseTime, "thresholdMs", 1000,
					"withinThreshold", responseTime < 1000));

			// Get collections with validation
			List<Map<String, Object>> collections = getCollectionsWithValidation();
			status.put("collections", collections);
			status.put("collectionCount", collections.size());

			// Get database statistics
			Map<String, Object> stats = getDatabaseStatistics();
			status.put("statistics", stats);

			// Check required collections
			validateRequiredCollections(collections);

			// Test write operation
			boolean canWrite = testWriteOperation();
			status.put("writePermission", canWrite);

			status.put("status", "HEALTHY");
			status.put("message", "Database is fully operational");

			logger.info("Database status check completed successfully");
			return ResponseEntity.ok(status);

		} catch (CustomExceptions.ResourceNotFoundBusinessException e) {
			logger.error("Required database collections not found", e);
			throw e;
		} catch (Exception e) {
			logger.error("Database status check failed", e);
			throw new CustomExceptions.DatabaseConnectionBusinessException("Failed to check database status", e);
		}
	}

	@GetMapping("/db/diagnostics")
	public Map<String, Object> databaseDiagnostics() {
		logger.debug("Database diagnostics endpoint accessed");

		if (mongoTemplate == null) {
			throw new CustomExceptions.ResourceNotFoundBusinessException("Database", "MongoDB is not configured");
		}

		try {
			Map<String, Object> diagnostics = new HashMap<>();
			diagnostics.put("timestamp", Instant.now());
			diagnostics.put("check", "MongoDB Diagnostics");
			diagnostics.put("requestId", UUID.randomUUID().toString());

			// Connection diagnostics
			diagnostics.put("connection", performConnectionDiagnostics());

			// Performance diagnostics
			diagnostics.put("performance", performDatabasePerformanceDiagnostics());

			// Schema diagnostics
			diagnostics.put("schema", performSchemaDiagnostics());

			diagnostics.put("status", "COMPLETE");
			diagnostics.put("message", "Database diagnostics collected successfully");

			return diagnostics;

		} catch (Exception e) {
			logger.error("Database diagnostics failed", e);
			throw new CustomExceptions.DatabaseConnectionBusinessException("Database diagnostics failed", e);
		}
	}

	@GetMapping("/db/info")
	public Map<String, Object> databaseInformation() {
		logger.debug("Database info endpoint accessed");
		return collectDatabaseInfo();
	}

	// ===========================================
	// TESTING & DEBUGGING ENDPOINTS
	// ===========================================

	@GetMapping("/test/connection")
	public Map<String, Object> testConnection() {
		logger.debug("Test connection endpoint accessed");

		Map<String, Object> result = new HashMap<>();
		result.put("test", "MongoDB Connection Test");
		result.put("timestamp", Instant.now());
		result.put("requestId", UUID.randomUUID().toString());

		if (mongoTemplate == null) {
			result.put("status", "FAILED");
			result.put("error", "MongoTemplate is not initialized");
			result.put("suggestion", "Check MongoDB configuration in application.properties");
			return result;
		}

		try {
			String dbName = mongoTemplate.getDb().getName();
			result.put("database", dbName);

			// Ping test
			long pingStart = System.currentTimeMillis();
			mongoTemplate.executeCommand("{ ping: 1 }");
			long pingTime = System.currentTimeMillis() - pingStart;
			result.put("ping", Map.of("status", "SUCCESS", "timeMs", pingTime));

			// Server status
			mongoTemplate.executeCommand("{ serverStatus: 1 }");
			result.put("serverStatus", "RETRIEVED");

			// Collections
			List<String> collections = new ArrayList<>();
			mongoTemplate.getDb().listCollectionNames().forEach(collections::add);
			result.put("collections", collections);
			result.put("collectionCount", collections.size());

			// Database stats
			mongoTemplate.executeCommand("{ dbStats: 1, scale: 1 }");
			result.put("dbStats", "RETRIEVED");

			result.put("status", "SUCCESS");
			result.put("message", "MongoDB connection is healthy");

			logger.info("Connection test successful for database: {}", dbName);

		} catch (Exception e) {
			result.put("status", "FAILED");
			result.put("error", e.getMessage());
			result.put("errorType", e.getClass().getSimpleName());
			result.put("suggestion", "Check MongoDB service and connection string");

			logger.error("Connection test failed: {}", e.getMessage(), e);
		}

		return result;
	}

	@GetMapping("/check/mongo")
	public Map<String, Object> checkMongoConfiguration() {
		logger.debug("Check Mongo configuration endpoint accessed");

		Map<String, Object> config = new HashMap<>();
		config.put("check", "MongoDB Configuration");
		config.put("timestamp", Instant.now());
		config.put("requestId", UUID.randomUUID().toString());

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

					// Test connection
					mongoTemplate.executeCommand("{ ping: 1 }");
					config.put("connectionTest", "SUCCESS");

				} catch (Exception e) {
					config.put("status", "CONFIGURED_BUT_ERROR");
					config.put("error", e.getMessage());
					config.put("connectionTest", "FAILED");
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

	// ===========================================
	// SYSTEM INFORMATION ENDPOINTS
	// ===========================================

	@GetMapping("/system/info")
	public Map<String, Object> systemInformation() {
		logger.debug("System info endpoint accessed");
		return collectSystemInfo();
	}

	@GetMapping("/app/info")
	public Map<String, Object> applicationInformation() {
		logger.debug("Application info endpoint accessed");
		return collectApplicationInfo();
	}

	@GetMapping("/memory/info")
	public Map<String, Object> memoryInformation() {
		logger.debug("Memory info endpoint accessed");
		return collectMemoryInfo();
	}

	@GetMapping("/system/status")
	public Map<String, Object> systemStatus() {
		logger.debug("System status endpoint accessed");

		Map<String, Object> status = new HashMap<>();
		status.put("timestamp", Instant.now());
		status.put("service", SERVICE_NAME);
		status.put("requestId", UUID.randomUUID().toString());

		try {
			status.put("memory", getDetailedMemoryStatus());
			status.put("cpu", getCPUStatus());
			status.put("threads", getThreadStatus());
			status.put("gc", getGCStatus());

			// Check for critical conditions
			Map<String, Object> memoryStatus = getDetailedMemoryStatus();
			double usagePercent = (Double) memoryStatus.get("usagePercent");

			if (usagePercent > 90) {
				status.put("overall", "CRITICAL");
				status.put("warning", "Memory usage is critical: " + String.format("%.1f%%", usagePercent));
				logger.warn("System status critical - Memory usage: {}%", usagePercent);
			} else if (usagePercent > 80) {
				status.put("overall", "WARNING");
				status.put("warning", "Memory usage is high: " + String.format("%.1f%%", usagePercent));
				logger.warn("System status warning - Memory usage: {}%", usagePercent);
			} else {
				status.put("overall", "HEALTHY");
				status.put("message", "System resources are within normal limits");
			}

		} catch (OutOfMemoryError  e) {
			status.put("overall", "CRITICAL");
			status.put("warning", e.getMessage());
			logger.warn("System status check critical: {}", e.getMessage());
		} catch (Exception e) {
			status.put("overall", "ERROR");
			status.put("error", e.getMessage());
			logger.error("System status check failed", e);
		}

		return status;
	}

	@GetMapping("/metrics")
	public Map<String, Object> metrics() {
		logger.debug("Metrics endpoint accessed");

		Map<String, Object> metrics = new HashMap<>();
		metrics.put("timestamp", Instant.now());
		metrics.put("service", SERVICE_NAME);
		metrics.put("requestId", UUID.randomUUID().toString());

		metrics.put("application", collectApplicationMetrics());
		metrics.put("database", collectDatabaseMetrics());
		metrics.put("performance", collectRealTimePerformanceMetrics());
		metrics.put("business", collectBusinessMetrics());

		return metrics;
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

	private Map<String, Object> checkCollectionsHealth() {
		Map<String, Object> collectionsHealth = new HashMap<>();
		collectionsHealth.put("component", "Database Collections");
		collectionsHealth.put("type", "database");

		if (mongoTemplate == null) {
			collectionsHealth.put("status", "UNKNOWN");
			collectionsHealth.put("message", "MongoDB not configured");
			return collectionsHealth;
		}

		try {
			List<String> required = Arrays.asList("manage_data", "user_registrations");
			List<String> found = new ArrayList<>();
			List<String> missing = new ArrayList<>();

			for (String collection : mongoTemplate.getDb().listCollectionNames()) {
				found.add(collection);
			}

			for (String requiredCollection : required) {
				if (!found.contains(requiredCollection)) {
					missing.add(requiredCollection);
				}
			}

			collectionsHealth.put("foundCollections", found);
			collectionsHealth.put("requiredCollections", required);
			collectionsHealth.put("missingCollections", missing);
			collectionsHealth.put("totalFound", found.size());

			if (missing.isEmpty()) {
				collectionsHealth.put("status", "HEALTHY");
				collectionsHealth.put("message", "All required collections present");
			} else {
				collectionsHealth.put("status", "UNHEALTHY");
				collectionsHealth.put("message", "Missing collections: " + String.join(", ", missing));
			}

		} catch (Exception e) {
			collectionsHealth.put("status", "ERROR");
			collectionsHealth.put("error", e.getMessage());
		}

		return collectionsHealth;
	}

	private Map<String, Object> checkAllFilesHealth() {
		Map<String, Object> filesHealth = new HashMap<>();
		filesHealth.put("component", "Required Files");
		filesHealth.put("type", "filesystem");

		boolean allFilesHealthy = true;
		List<String> missingFiles = new ArrayList<>();
		List<String> unhealthyFiles = new ArrayList<>();
		Map<String, Object> fileStatuses = new HashMap<>();

		for (String fileName : REQUIRED_FILES) {
			Map<String, Object> fileStatus = checkFileHealth(fileName);
			fileStatuses.put(fileName, fileStatus);

			String status = (String) fileStatus.get("status");
			if ("MISSING".equals(status)) {
				allFilesHealthy = false;
				missingFiles.add(fileName);
			} else if ("ERROR".equals(status) || "UNREADABLE".equals(status)) {
				allFilesHealthy = false;
				unhealthyFiles.add(fileName);
			}
		}

		filesHealth.put("files", fileStatuses);
		filesHealth.put("totalFilesChecked", REQUIRED_FILES.size());
		filesHealth.put("allFilesHealthy", allFilesHealthy);
		filesHealth.put("overallStatus", allFilesHealthy ? "HEALTHY" : "UNHEALTHY");

		if (!missingFiles.isEmpty()) {
			filesHealth.put("missingFiles", missingFiles);
			filesHealth.put("missingCount", missingFiles.size());
		}

		if (!unhealthyFiles.isEmpty()) {
			filesHealth.put("unhealthyFiles", unhealthyFiles);
			filesHealth.put("unhealthyCount", unhealthyFiles.size());
		}

		return filesHealth;
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

		app.put("name", SERVICE_NAME);
		app.put("version", SERVICE_VERSION);
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
			db.put("type", "MongoDB");

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
		metrics.put("timestamp", Instant.now());

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
		deps.put("mongodb", "MongoDB Driver");

		Map<String, String> dependencyChecks = new HashMap<>();

		checkDependency("Spring Data MongoDB", "org.springframework.data.mongodb.core.MongoTemplate", dependencyChecks);
		checkDependency("Spring Web", "org.springframework.web.bind.annotation.RestController", dependencyChecks);
		checkDependency("Jackson JSON", "com.fasterxml.jackson.databind.ObjectMapper", dependencyChecks);
		checkDependency("SLF4J", "org.slf4j.Logger", dependencyChecks);

		deps.put("availableDependencies", dependencyChecks);

		return deps;
	}

	private Map<String, Object> collectEnvironmentInfo() {
		Map<String, Object> env = new HashMap<>();
		env.put("activeProfiles", getActiveProfiles());
		env.put("serverPort", environment.getProperty("server.port", "8080"));
		env.put("applicationName", environment.getProperty("spring.application.name", SERVICE_NAME));
		return env;
	}

	// ===========================================
	// HELPER METHODS FOR ADVANCED CHECKS
	// ===========================================

	private Map<String, Object> performDatabaseHealthCheck() {
		return checkMongoDBHealth();
	}

	private Map<String, Object> performMemoryHealthCheck() {
		return checkMemoryHealth();
	}

	private Map<String, Object> performApplicationHealthCheck() {
		return checkApplicationHealth();
	}

	private Map<String, Object> performCollectionsHealthCheck() {
		return checkCollectionsHealth();
	}

	private Map<String, Object> performConnectivityHealthCheck() {
		Map<String, Object> connectivity = new HashMap<>();
		connectivity.put("component", "Connectivity");
		connectivity.put("type", "network");

		try {
			// Test local connectivity
			connectivity.put("localhost", "REACHABLE");

			if (mongoTemplate != null) {
				long start = System.currentTimeMillis();
				mongoTemplate.executeCommand("{ ping: 1 }");
				long latency = System.currentTimeMillis() - start;
				connectivity.put("mongodb", Map.of("status", "REACHABLE", "latencyMs", latency));
			} else {
				connectivity.put("mongodb", "NOT_CONFIGURED");
			}

			connectivity.put("status", "HEALTHY");
			connectivity.put("message", "Connectivity checks passed");

		} catch (Exception e) {
			connectivity.put("status", "UNHEALTHY");
			connectivity.put("error", e.getMessage());
		}

		return connectivity;
	}

	private List<Map<String, Object>> getCollectionsWithValidation() {
		List<Map<String, Object>> collections = new ArrayList<>();

		if (mongoTemplate == null) {
			return collections;
		}

		for (String collectionName : mongoTemplate.getDb().listCollectionNames()) {
			Map<String, Object> collectionInfo = new HashMap<>();
			collectionInfo.put("name", collectionName);

			try {
				long count = mongoTemplate.getCollection(collectionName).countDocuments();
				collectionInfo.put("documentCount", count);
				collectionInfo.put("status", "VALID");
			} catch (Exception e) {
				collectionInfo.put("status", "ERROR");
				collectionInfo.put("error", e.getMessage());
			}

			collections.add(collectionInfo);
		}

		return collections;
	}

	private void validateRequiredCollections(List<Map<String, Object>> collections) {
		List<String> required = Arrays.asList("manage_data", "user_registrations");
		List<String> missing = new ArrayList<>();

		List<String> foundNames = new ArrayList<>();
		for (Map<String, Object> collection : collections) {
			foundNames.add(collection.get("name").toString());
		}

		for (String requiredCollection : required) {
			if (!foundNames.contains(requiredCollection)) {
				missing.add(requiredCollection);
			}
		}

		if (!missing.isEmpty()) {
			throw new CustomExceptions.ResourceNotFoundBusinessException("Database Collections",
					String.format("Required collections missing: %s", String.join(", ", missing)));
		}
	}

	private boolean testWriteOperation() {
		try {
			// Create a test document in a temporary collection
			String testCollection = "health_check_test_" + System.currentTimeMillis();
			Map<String, Object> testDoc = new HashMap<>();
			testDoc.put("test", true);
			testDoc.put("timestamp", Instant.now());
			testDoc.put("service", SERVICE_NAME);

			mongoTemplate.save(testDoc, testCollection);

			// Clean up
			mongoTemplate.dropCollection(testCollection);

			return true;
		} catch (Exception e) {
			logger.warn("Write operation test failed: {}", e.getMessage());
			return false;
		}
	}

	private Map<String, Object> getDatabaseStatistics() {
		Map<String, Object> stats = new HashMap<>();
		try {
			if (mongoTemplate != null) {
				List<String> collections = new ArrayList<>();
				mongoTemplate.getDb().listCollectionNames().into(collections);
				stats.put("collectionCount", collections.size());

				// Try to get db stats
				try {
					mongoTemplate.executeCommand("{ dbStats: 1, scale: 1 }");
					stats.put("dbStats", "AVAILABLE");
				} catch (Exception e) {
					stats.put("dbStats", "UNAVAILABLE");
				}
			}
		} catch (Exception e) {
			stats.put("error", e.getMessage());
		}
		return stats;
	}

	private Map<String, Object> getDetailedMemoryStatus() {
		Map<String, Object> memory = new HashMap<>();
		Runtime runtime = Runtime.getRuntime();
		long max = runtime.maxMemory();
		long total = runtime.totalMemory();
		long free = runtime.freeMemory();
		long used = total - free;
		double percent = (used * 100.0) / max;

		memory.put("max", formatBytes(max));
		memory.put("total", formatBytes(total));
		memory.put("free", formatBytes(free));
		memory.put("used", formatBytes(used));
		memory.put("usagePercent", percent);
		memory.put("usagePercentFormatted", String.format("%.1f%%", percent));
		memory.put("status", percent > 90 ? "CRITICAL" : percent > 80 ? "WARNING" : "OK");

		return memory;
	}

	private Map<String, Object> getCPUStatus() {
		Map<String, Object> cpu = new HashMap<>();
		cpu.put("availableProcessors", Runtime.getRuntime().availableProcessors());
		cpu.put("status", "OK");
		return cpu;
	}

	private Map<String, Object> getThreadStatus() {
		Map<String, Object> threads = new HashMap<>();
		threads.put("activeThreads", Thread.activeCount());
		threads.put("status", "OK");
		return threads;
	}

	private Map<String, Object> getGCStatus() {
		Map<String, Object> gc = new HashMap<>();
		try {
			List<Map<String, Object>> gcs = new ArrayList<>();
			for (java.lang.management.GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
				Map<String, Object> gcInfo = new HashMap<>();
				gcInfo.put("name", gcBean.getName());
				gcInfo.put("collectionCount", gcBean.getCollectionCount());
				gcInfo.put("collectionTime", gcBean.getCollectionTime());
				gcs.add(gcInfo);
			}
			gc.put("garbageCollectors", gcs);
			gc.put("status", "OK");
		} catch (Exception e) {
			gc.put("status", "ERROR");
			gc.put("error", e.getMessage());
		}
		return gc;
	}

	private Map<String, Object> collectApplicationMetrics() {
		Map<String, Object> metrics = new HashMap<>();
		metrics.put("uptime", getFormattedUptime());
		metrics.put("memory", getDetailedMemoryStatus());
		metrics.put("threads", getThreadStatus());
		return metrics;
	}

	private Map<String, Object> collectDatabaseMetrics() {
		Map<String, Object> metrics = new HashMap<>();
		if (mongoTemplate != null) {
			try {
				metrics.put("database", mongoTemplate.getDb().getName());

				long start = System.currentTimeMillis();
				mongoTemplate.executeCommand("{ ping: 1 }");
				long latency = System.currentTimeMillis() - start;

				metrics.put("ping", Map.of("latencyMs", latency, "status", latency < 1000 ? "OK" : "SLOW"));

				List<String> collections = new ArrayList<>();
				mongoTemplate.getDb().listCollectionNames().into(collections);
				metrics.put("collectionCount", collections.size());

			} catch (Exception e) {
				metrics.put("error", e.getMessage());
			}
		}
		return metrics;
	}

	private Map<String, Object> collectRealTimePerformanceMetrics() {
		Map<String, Object> metrics = new HashMap<>();
		metrics.put("timestamp", Instant.now());
		metrics.put("threadCount", Thread.activeCount());
		metrics.put("memory", getDetailedMemoryStatus());
		return metrics;
	}

	private Map<String, Object> collectBusinessMetrics() {
		Map<String, Object> metrics = new HashMap<>();
		metrics.put("service", SERVICE_NAME);
		metrics.put("version", SERVICE_VERSION);
		metrics.put("timestamp", Instant.now());
		// Add business-specific metrics here as needed
		return metrics;
	}

	private Map<String, Object> performConnectionDiagnostics() {
		Map<String, Object> diagnostics = new HashMap<>();
		if (mongoTemplate != null) {
			try {
				long start = System.currentTimeMillis();
				mongoTemplate.executeCommand("{ ping: 1 }");
				long latency = System.currentTimeMillis() - start;
				diagnostics.put("latencyMs", latency);
				diagnostics.put("status", "OK");
				diagnostics.put("message", latency < 1000 ? "Within acceptable range" : "High latency");
			} catch (Exception e) {
				diagnostics.put("status", "ERROR");
				diagnostics.put("error", e.getMessage());
			}
		}
		return diagnostics;
	}

	private Map<String, Object> performDatabasePerformanceDiagnostics() {
		Map<String, Object> diagnostics = new HashMap<>();
		if (mongoTemplate != null) {
			try {
				// Simple performance test
				long start = System.currentTimeMillis();
				List<String> collections = new ArrayList<>();
				mongoTemplate.getDb().listCollectionNames().into(collections);
				long time = System.currentTimeMillis() - start;

				diagnostics.put("collectionCount", collections.size());
				diagnostics.put("listCollectionsTimeMs", time);
				diagnostics.put("status", "OK");
				diagnostics.put("performance", time < 500 ? "GOOD" : time < 1000 ? "ACCEPTABLE" : "SLOW");
			} catch (Exception e) {
				diagnostics.put("status", "ERROR");
				diagnostics.put("error", e.getMessage());
			}
		}
		return diagnostics;
	}

	private Map<String, Object> performSchemaDiagnostics() {
		Map<String, Object> diagnostics = new HashMap<>();
		if (mongoTemplate != null) {
			try {
				List<String> collections = new ArrayList<>();
				mongoTemplate.getDb().listCollectionNames().into(collections);
				diagnostics.put("collections", collections);
				diagnostics.put("count", collections.size());
				diagnostics.put("status", "OK");
			} catch (Exception e) {
				diagnostics.put("status", "ERROR");
				diagnostics.put("error", e.getMessage());
			}
		}
		return diagnostics;
	}

	// ===========================================
	// PRIVATE FILE HEALTH CHECK METHODS
	// ===========================================

	private Map<String, Object> checkFileHealth(String fileName) {
		Map<String, Object> status = new HashMap<>();
		status.put("name", fileName);
		status.put("description", FILE_DESCRIPTIONS.getOrDefault(fileName, "Configuration file"));
		status.put("required", true);

		try {
			Path filePath = getFilePath(fileName);

			if (Files.exists(filePath)) {
				if (Files.isReadable(filePath)) {
					BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
					long size = attrs.size();
					FileTime lastModified = attrs.lastModifiedTime();

					status.put("status", "HEALTHY");
					status.put("size", size);
					status.put("sizeFormatted", formatBytes(size));
					status.put("lastModified", Instant.ofEpochMilli(lastModified.toMillis()));
					status.put("lastModifiedFormatted", lastModified.toString());
					status.put("exists", true);
					status.put("readable", true);
					status.put("writable", Files.isWritable(filePath));
					status.put("executable", Files.isExecutable(filePath));

					// Additional checks based on file type
					if (fileName.equals("application.properties")) {
						status.putAll(checkApplicationPropertiesContent(filePath));
					} else if (fileName.equals("render.yaml")) {
						status.putAll(checkRenderYamlContent(filePath));
					} else if (fileName.equals("Dockerfile")) {
						status.putAll(checkDockerfileContent(filePath));
					}

				} else {
					status.put("status", "UNREADABLE");
					status.put("exists", true);
					status.put("readable", false);
					status.put("error", "File exists but is not readable");
					status.put("severity", "ERROR");
				}
			} else {
				status.put("status", "MISSING");
				status.put("exists", false);
				status.put("error", "Required file is missing");
				status.put("severity", "CRITICAL");
			}

		} catch (Exception e) {
			status.put("status", "ERROR");
			status.put("error", e.getMessage());
			status.put("severity", "ERROR");
			status.put("exception", e.getClass().getName());
		}

		return status;
	}

	private Map<String, Object> getFileInfo(String fileName) {
		Map<String, Object> info = new HashMap<>();
		info.put("name", fileName);
		info.put("description", FILE_DESCRIPTIONS.getOrDefault(fileName, "Configuration file"));

		try {
			Path filePath = getFilePath(fileName);

			if (Files.exists(filePath)) {
				BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
				FileTime creationTime = attrs.creationTime();
				FileTime lastModified = attrs.lastModifiedTime();
				FileTime lastAccess = attrs.lastAccessTime();

				info.put("status", "PRESENT");
				info.put("path", filePath.toAbsolutePath().toString());
				info.put("size", attrs.size());
				info.put("sizeFormatted", formatBytes(attrs.size()));
				info.put("creationTime", Instant.ofEpochMilli(creationTime.toMillis()));
				info.put("lastModified", Instant.ofEpochMilli(lastModified.toMillis()));
				info.put("lastAccess", Instant.ofEpochMilli(lastAccess.toMillis()));
				info.put("isRegularFile", attrs.isRegularFile());
				info.put("isDirectory", attrs.isDirectory());
				info.put("isSymbolicLink", attrs.isSymbolicLink());

				// Calculate file age
				Duration age = Duration.between(Instant.ofEpochMilli(lastModified.toMillis()), Instant.now());
				info.put("age", formatDuration(age));
				info.put("ageDays", age.toDays());

			} else {
				info.put("status", "MISSING");
				info.put("error", "File does not exist");
				info.put("severity", "HIGH");
			}

		} catch (Exception e) {
			info.put("status", "ERROR");
			info.put("error", e.getMessage());
		}

		return info;
	}

	private Map<String, Object> getDetailedFileInfo(String fileName) {
		Map<String, Object> details = new HashMap<>();
		details.put("name", fileName);
		details.put("description", FILE_DESCRIPTIONS.getOrDefault(fileName, "Configuration file"));

		try {
			Path filePath = getFilePath(fileName);

			if (Files.exists(filePath)) {
				// Basic attributes
				BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);

				details.put("path", filePath.toAbsolutePath().toString());
				details.put("parent", filePath.getParent() != null ? filePath.getParent().toString() : "N/A");
				details.put("fileName", filePath.getFileName().toString());
				details.put("size", attrs.size());
				details.put("sizeFormatted", formatBytes(attrs.size()));

				// Timestamps
				Map<String, Object> timestamps = new HashMap<>();
				timestamps.put("creation", Instant.ofEpochMilli(attrs.creationTime().toMillis()));
				timestamps.put("lastModified", Instant.ofEpochMilli(attrs.lastModifiedTime().toMillis()));
				timestamps.put("lastAccess", Instant.ofEpochMilli(attrs.lastAccessTime().toMillis()));
				details.put("timestamps", timestamps);

				// Permissions
				Map<String, Boolean> permissions = new HashMap<>();
				permissions.put("readable", Files.isReadable(filePath));
				permissions.put("writable", Files.isWritable(filePath));
				permissions.put("executable", Files.isExecutable(filePath));
				permissions.put("hidden", Files.isHidden(filePath));
				details.put("permissions", permissions);

				// File type info
				Map<String, Object> typeInfo = new HashMap<>();
				typeInfo.put("isRegularFile", attrs.isRegularFile());
				typeInfo.put("isDirectory", attrs.isDirectory());
				typeInfo.put("isSymbolicLink", attrs.isSymbolicLink());
				typeInfo.put("isOther", attrs.isOther());
				details.put("type", typeInfo);

				// Content analysis (for text files)
				if (fileName.endsWith(".properties") || fileName.endsWith(".yaml") || fileName.equals("Dockerfile")
						|| fileName.endsWith(".yml")) {

					try {
						List<String> lines = Files.readAllLines(filePath);
						details.put("lineCount", lines.size());

						// Calculate approximate character count
						long charCount = lines.stream().mapToLong(String::length).sum();
						details.put("characterCount", charCount);

						// For specific file types, do content analysis
						if (fileName.equals("application.properties")) {
							long propertyCount = lines.stream()
									.filter(line -> line.contains("=") && !line.trim().startsWith("#")).count();
							details.put("propertyCount", propertyCount);

						} else if (fileName.equals("Dockerfile")) {
							long instructionCount = lines.stream()
									.filter(line -> line.matches("^(FROM|RUN|COPY|WORKDIR|EXPOSE|ENTRYPOINT|CMD).*"))
									.count();
							details.put("instructionCount", instructionCount);
						}

					} catch (Exception e) {
						details.put("contentAnalysis", "Failed: " + e.getMessage());
					}
				}

				details.put("status", "ANALYZED");
				details.put("message", "File analysis completed successfully");

			} else {
				details.put("status", "MISSING");
				details.put("error", "File not found in expected location");
				details.put("suggestedAction", "Verify file exists in project root directory");
			}

		} catch (Exception e) {
			details.put("status", "ERROR");
			details.put("error", e.getMessage());
			details.put("exception", e.getClass().getName());
		}

		return details;
	}

	private Map<String, Object> checkApplicationProperties() {
		Map<String, Object> check = new HashMap<>();
		check.put("file", "application.properties");
		check.put("purpose", "Spring Boot application configuration");
		check.put("required", true);

		try {
			Path filePath = getFilePath("application.properties");

			if (!Files.exists(filePath)) {
				check.put("status", "MISSING");
				check.put("severity", "CRITICAL");
				check.put("message", "application.properties is required for Spring Boot configuration");
				return check;
			}

			if (!Files.isReadable(filePath)) {
				check.put("status", "UNREADABLE");
				check.put("severity", "CRITICAL");
				check.put("message", "Cannot read application.properties");
				return check;
			}

			List<String> lines = Files.readAllLines(filePath);
			check.put("lineCount", lines.size());

			// Check for critical properties
			List<String> criticalProperties = Arrays.asList("spring.application.name", "server.port",
					"spring.data.mongodb.uri");

			List<String> missingProperties = new ArrayList<>();
			Map<String, String> foundProperties = new HashMap<>();

			for (String prop : criticalProperties) {
				boolean found = lines.stream().anyMatch(line -> line.trim().startsWith(prop + "="));

				if (found) {
					foundProperties.put(prop, "PRESENT");
				} else {
					missingProperties.add(prop);
					foundProperties.put(prop, "MISSING");
				}
			}

			check.put("criticalProperties", foundProperties);

			if (missingProperties.isEmpty()) {
				check.put("status", "VALID");
				check.put("severity", "INFO");
				check.put("message", "All critical properties are configured");
			} else {
				check.put("status", "INCOMPLETE");
				check.put("severity", "WARNING");
				check.put("message", "Missing critical properties: " + String.join(", ", missingProperties));
				check.put("missingProperties", missingProperties);
			}

		} catch (Exception e) {
			check.put("status", "ERROR");
			check.put("severity", "ERROR");
			check.put("message", "Error checking application.properties: " + e.getMessage());
		}

		return check;
	}

	private Map<String, Object> checkRenderYaml() {
		Map<String, Object> check = new HashMap<>();
		check.put("file", "render.yaml");
		check.put("purpose", "Render deployment configuration");
		check.put("required", true);

		try {
			Path filePath = getFilePath("render.yaml");

			if (!Files.exists(filePath)) {
				check.put("status", "MISSING");
				check.put("severity", "HIGH");
				check.put("message", "render.yaml is required for Render deployment");
				return check;
			}

			if (!Files.isReadable(filePath)) {
				check.put("status", "UNREADABLE");
				check.put("severity", "HIGH");
				check.put("message", "Cannot read render.yaml");
				return check;
			}

			List<String> lines = Files.readAllLines(filePath);
			check.put("lineCount", lines.size());

			// Basic YAML structure check
			boolean hasServices = lines.stream().anyMatch(line -> line.trim().startsWith("services:"));
			boolean hasDockerConfig = lines.stream()
					.anyMatch(line -> line.contains("dockerfilePath") || line.contains("dockerContext"));
			boolean hasEnvVars = lines.stream().anyMatch(line -> line.contains("envVars:"));

			Map<String, Boolean> structureChecks = new HashMap<>();
			structureChecks.put("hasServicesSection", hasServices);
			structureChecks.put("hasDockerConfig", hasDockerConfig);
			structureChecks.put("hasEnvVars", hasEnvVars);
			check.put("structureChecks", structureChecks);

			int passingChecks = (int) structureChecks.values().stream().filter(Boolean::booleanValue).count();

			if (passingChecks == structureChecks.size()) {
				check.put("status", "VALID");
				check.put("severity", "INFO");
				check.put("message", "Render YAML configuration appears valid");
			} else if (passingChecks > 0) {
				check.put("status", "PARTIAL");
				check.put("severity", "WARNING");
				check.put("message", "Render YAML has incomplete configuration");
			} else {
				check.put("status", "INVALID");
				check.put("severity", "ERROR");
				check.put("message", "Render YAML does not appear to be properly structured");
			}

		} catch (Exception e) {
			check.put("status", "ERROR");
			check.put("severity", "ERROR");
			check.put("message", "Error checking render.yaml: " + e.getMessage());
		}

		return check;
	}

	private Map<String, Object> checkDockerfile() {
		Map<String, Object> check = new HashMap<>();
		check.put("file", "Dockerfile");
		check.put("purpose", "Docker container definition");
		check.put("required", true);

		try {
			Path filePath = getFilePath("Dockerfile");

			if (!Files.exists(filePath)) {
				check.put("status", "MISSING");
				check.put("severity", "HIGH");
				check.put("message", "Dockerfile is required for containerized deployment");
				return check;
			}

			if (!Files.isReadable(filePath)) {
				check.put("status", "UNREADABLE");
				check.put("severity", "HIGH");
				check.put("message", "Cannot read Dockerfile");
				return check;
			}

			List<String> lines = Files.readAllLines(filePath);
			check.put("lineCount", lines.size());

			// Check for required Docker instructions
			boolean hasFrom = lines.stream().anyMatch(line -> line.trim().startsWith("FROM "));
			boolean hasWorkdir = lines.stream().anyMatch(line -> line.trim().startsWith("WORKDIR "));
			boolean hasCopy = lines.stream().anyMatch(line -> line.trim().startsWith("COPY "));
			boolean hasExpose = lines.stream().anyMatch(line -> line.trim().startsWith("EXPOSE "));
			boolean hasEntrypoint = lines.stream()
					.anyMatch(line -> line.trim().startsWith("ENTRYPOINT ") || line.trim().startsWith("CMD "));

			Map<String, Boolean> instructionChecks = new HashMap<>();
			instructionChecks.put("hasFROM", hasFrom);
			instructionChecks.put("hasWORKDIR", hasWorkdir);
			instructionChecks.put("hasCOPY", hasCopy);
			instructionChecks.put("hasEXPOSE", hasExpose);
			instructionChecks.put("hasENTRYPOINT_or_CMD", hasEntrypoint);
			check.put("instructionChecks", instructionChecks);

			int passingChecks = (int) instructionChecks.values().stream().filter(Boolean::booleanValue).count();

			if (passingChecks == instructionChecks.size()) {
				check.put("status", "VALID");
				check.put("severity", "INFO");
				check.put("message", "Dockerfile appears to be well-structured");
			} else if (passingChecks >= 3) { // At least basic instructions
				check.put("status", "BASIC");
				check.put("severity", "WARNING");
				check.put("message", "Dockerfile has basic structure but may be missing some instructions");
			} else {
				check.put("status", "INCOMPLETE");
				check.put("severity", "ERROR");
				check.put("message", "Dockerfile appears to be incomplete or malformed");
			}

		} catch (Exception e) {
			check.put("status", "ERROR");
			check.put("severity", "ERROR");
			check.put("message", "Error checking Dockerfile: " + e.getMessage());
		}

		return check;
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
		return environment.getActiveProfiles();
	}

	private void checkDependency(String name, String className, Map<String, String> results) {
		try {
			Class.forName(className);
			results.put(name, "AVAILABLE");
		} catch (ClassNotFoundException e) {
			results.put(name, "MISSING");
		}
	}

	// Helper method to get file path
	private Path getFilePath(String fileName) {
		// Try to find the file in various locations
		List<Path> possiblePaths = Arrays.asList(Paths.get(fileName), // Current directory
				Paths.get("./" + fileName), // Current directory with ./
				Paths.get("src/main/resources/" + fileName), // Resources directory
				Paths.get(System.getProperty("user.dir"), fileName) // User directory
		);

		for (Path path : possiblePaths) {
			if (Files.exists(path)) {
				return path;
			}
		}

		// Return the most likely path if file doesn't exist
		return Paths.get(fileName);
	}

	// Helper method to format duration
	private String formatDuration(Duration duration) {
		long days = duration.toDays();
		long hours = duration.toHours() % 24;
		long minutes = duration.toMinutes() % 60;
		long seconds = duration.getSeconds() % 60;

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

	// Content checking methods
	private Map<String, Object> checkApplicationPropertiesContent(Path filePath) {
		Map<String, Object> contentChecks = new HashMap<>();
		try {
			List<String> lines = Files.readAllLines(filePath);

			// Check for MongoDB URI (should be present and not empty)
			boolean hasMongoUri = lines.stream().anyMatch(line -> line.trim().startsWith("spring.data.mongodb.uri=")
					&& line.trim().length() > "spring.data.mongodb.uri=".length());

			// Check for server port configuration
			boolean hasServerPort = lines.stream().anyMatch(line -> line.trim().startsWith("server.port="));

			// Check for application name
			boolean hasAppName = lines.stream().anyMatch(line -> line.trim().startsWith("spring.application.name="));

			contentChecks.put("hasMongoDBUri", hasMongoUri);
			contentChecks.put("hasServerPort", hasServerPort);
			contentChecks.put("hasApplicationName", hasAppName);
			contentChecks.put("contentChecksPassed", hasMongoUri && hasServerPort && hasAppName);

		} catch (Exception e) {
			contentChecks.put("contentCheckError", e.getMessage());
		}
		return contentChecks;
	}

	private Map<String, Object> checkRenderYamlContent(Path filePath) {
		Map<String, Object> contentChecks = new HashMap<>();
		try {
			List<String> lines = Files.readAllLines(filePath);
			String content = String.join("\n", lines);

			// Check for Docker configuration
			boolean hasDockerfilePath = content.contains("dockerfilePath:");
			boolean hasDockerContext = content.contains("dockerContext:");
			boolean hasEnvVars = content.contains("envVars:");
			boolean hasHealthCheck = content.contains("healthCheckPath:");

			contentChecks.put("hasDockerfilePath", hasDockerfilePath);
			contentChecks.put("hasDockerContext", hasDockerContext);
			contentChecks.put("hasEnvVars", hasEnvVars);
			contentChecks.put("hasHealthCheck", hasHealthCheck);

			// Count services
			long serviceCount = lines.stream().filter(line -> line.trim().startsWith("- type:")).count();
			contentChecks.put("serviceCount", serviceCount);

		} catch (Exception e) {
			contentChecks.put("contentCheckError", e.getMessage());
		}
		return contentChecks;
	}

	private Map<String, Object> checkDockerfileContent(Path filePath) {
		Map<String, Object> contentChecks = new HashMap<>();
		try {
			List<String> lines = Files.readAllLines(filePath);

			// Check for multi-stage build
			boolean hasMultiStage = lines.stream().filter(line -> line.trim().startsWith("FROM ")).count() > 1;

			// Check for Java/JAR references
			boolean hasJava = lines.stream()
					.anyMatch(line -> line.toLowerCase().contains("java") || line.contains(".jar"));

			// Check for EXPOSE instruction
			boolean hasExpose = lines.stream().anyMatch(line -> line.trim().startsWith("EXPOSE "));

			// Check for ENTRYPOINT or CMD
			boolean hasEntrypoint = lines.stream().anyMatch(line -> line.trim().startsWith("ENTRYPOINT "));
			boolean hasCmd = lines.stream().anyMatch(line -> line.trim().startsWith("CMD "));

			contentChecks.put("hasMultiStage", hasMultiStage);
			contentChecks.put("hasJavaReference", hasJava);
			contentChecks.put("hasExpose", hasExpose);
			contentChecks.put("hasEntrypointOrCmd", hasEntrypoint || hasCmd);
			contentChecks.put("isSpringBootApp", hasJava && (hasExpose || hasEntrypoint || hasCmd));

		} catch (Exception e) {
			contentChecks.put("contentCheckError", e.getMessage());
		}
		return contentChecks;
	}
}