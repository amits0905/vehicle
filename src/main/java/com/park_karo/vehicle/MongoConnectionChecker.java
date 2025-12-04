package com.park_karo.vehicle;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class MongoConnectionChecker implements CommandLineRunner {

    // 1. Declare and initialize the logger instance
    private static final Logger logger = LoggerFactory.getLogger(MongoConnectionChecker.class);

	private final MongoTemplate mongoTemplate;

	// Spring injects the MongoTemplate, which guarantees a successful connection
	public MongoConnectionChecker(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public void run(String... args) throws Exception {
		try {
			// Attempt a simple operation to confirm the connection is active
			mongoTemplate.getDb().listCollectionNames().first();

			// SUCCESS MESSAGES: Use logger.info() for all output
			logger.info("==========================================================");
			// Concatenate the database name into the logger message
			logger.info("✅ MongoDB is connected to database: {}", mongoTemplate.getDb().getName()); 
			logger.info("==========================================================");

		} catch (Exception e) {
			// WARNING/ERROR: Use logger.error() for error output
			logger.error("===========================================================");
			logger.error("❌ WARNING: Database connection check failed after startup!");
			// Use SLF4J's placeholder {} to include the message, ensuring it's logged properly
			logger.error("❌ Error: {}", e.getMessage(), e); 
			logger.error("===========================================================");
		}
	}
}