package com.park_karo.vehicle.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mongodb.client.MongoClient;

@Component
public class MongoConnectionChecker implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(MongoConnectionChecker.class);
	private final MongoTemplate mongoTemplate;

	// Inject only MongoTemplate (simpler, works better)
	public MongoConnectionChecker(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public void run(String... args) throws Exception {
		logger.info("==========================================================");
		logger.info("Testing MongoDB connection...");

		try {
			// Test MongoDB connection with ping command
			mongoTemplate.executeCommand("{ ping: 1 }");
			String dbName = mongoTemplate.getDb().getName();

			logger.info("✅ SUCCESS: MongoDB connection established");
			logger.info("✅ Database: {}", dbName);
			logger.info("==========================================================");

		} catch (Exception e) {
			logger.error("===========================================================");
			logger.error("❌ FAILED: MongoDB connection could not be established");
			logger.error("❌ Error Type: {}", e.getClass().getSimpleName());
			logger.error("❌ Error Message: {}", e.getMessage());
			logger.error("===========================================================");

			// Optionally re-throw to fail fast on startup
			throw new RuntimeException("MongoDB connection failed on startup", e);
		}
	}
}