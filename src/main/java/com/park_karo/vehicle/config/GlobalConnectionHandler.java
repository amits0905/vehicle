package com.park_karo.vehicle.config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.park_karo.vehicle.exception.CustomExceptions;

@Component
public class GlobalConnectionHandler implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(GlobalConnectionHandler.class);
	private final MongoTemplate mongoTemplate;

	public GlobalConnectionHandler(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public void run(String... args) throws Exception {
		testAllConnections();
	}

	public void testAllConnections() {
		logger.info("===========================================");
		logger.info("🚀 Starting Global Connection Test...");
		logger.info("🚀 Timestamp: {}", new Date());
		logger.info("===========================================");

		testMongoDBConnection();
		testDatabaseCollections();
		testDatabaseHealth();

		logger.info("===========================================");
		logger.info("✅ Global Connection Test COMPLETE");
		logger.info("===========================================");
	}

	private void testMongoDBConnection() {
		try {
			logger.info("🔍 Testing MongoDB Connection...");

			String dbName = mongoTemplate.getDb().getName();
			logger.info("✅ MongoDB Connected to Database: {}", dbName);

			mongoTemplate.executeCommand("{ ping: 1 }");
			logger.info("✅ MongoDB Ping Successful");

			mongoTemplate.executeCommand("{ serverStatus: 1 }");
			logger.info("✅ MongoDB Server Status Retrieved");

		} catch (Exception e) {
			logger.error("❌ MongoDB Connection FAILED: {}", e.getMessage(), e);
			throw new CustomExceptions.DatabaseConnectionBusinessException("MongoDB connection failed on startup", e);
		}
	}

	private void testDatabaseCollections() {
		try {
			logger.info("🔍 Checking Database Collections...");

			Iterable<String> collections = mongoTemplate.getDb().listCollectionNames();
			int collectionCount = 0;

			for (String collection : collections) {
				logger.info("📁 Collection Found: {}", collection);
				collectionCount++;

				try {
					long documentCount = mongoTemplate.getCollection(collection).countDocuments();
					logger.info("   📄 Document Count: {}", documentCount);
				} catch (Exception e) {
					logger.warn("   ⚠️ Could not count documents for collection: {}", collection);
				}
			}

			if (collectionCount == 0) {
				logger.warn("⚠️ No collections found in database");
			} else {
				logger.info("✅ Found {} collections in database", collectionCount);
			}

		} catch (Exception e) {
			logger.error("❌ Failed to check collections: {}", e.getMessage());
		}
	}

	private void testDatabaseHealth() {
		try {
			logger.info("🔍 Checking Database Health...");

			mongoTemplate.executeCommand("{ dbStats: 1, scale: 1 }");
			logger.info("✅ Database Statistics Retrieved");

			try {
				mongoTemplate.executeCommand("{ connPoolStats: 1 }");
				logger.info("✅ Connection Pool Stats Retrieved");
			} catch (Exception e) {
				logger.debug("Connection pool stats not available: {}", e.getMessage());
			}

			logger.info("✅ Database Health Check PASSED");

		} catch (Exception e) {
			logger.error("❌ Database Health Check FAILED: {}", e.getMessage());
		}
	}

	public ConnectionStatus testConnection() {
		ConnectionStatus status = new ConnectionStatus();
		status.setTimestamp(new Date());

		try {
			String dbName = mongoTemplate.getDb().getName();
			status.setMongoStatus("CONNECTED");
			status.setDatabase(dbName);

			List<String> collectionList = new ArrayList<String>();
			mongoTemplate.getDb().listCollectionNames().into(collectionList);
			status.setCollectionCount(collectionList.size());

			mongoTemplate.executeCommand("{ ping: 1 }");
			status.setPingStatus("OK");

			status.setOverallStatus("HEALTHY");

		} catch (Exception e) {
			status.setOverallStatus("UNHEALTHY");
			status.setMongoStatus("ERROR");
			status.setErrorMessage(e.getMessage());
		}

		return status;
	}

	public static class ConnectionStatus {
		private Date timestamp;
		private String overallStatus;
		private String mongoStatus;
		private String database;
		private int collectionCount;
		private String pingStatus;
		private String errorMessage;

		public Date getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
		}

		public String getOverallStatus() {
			return overallStatus;
		}

		public void setOverallStatus(String overallStatus) {
			this.overallStatus = overallStatus;
		}

		public String getMongoStatus() {
			return mongoStatus;
		}

		public void setMongoStatus(String mongoStatus) {
			this.mongoStatus = mongoStatus;
		}

		public String getDatabase() {
			return database;
		}

		public void setDatabase(String database) {
			this.database = database;
		}

		public int getCollectionCount() {
			return collectionCount;
		}

		public void setCollectionCount(int collectionCount) {
			this.collectionCount = collectionCount;
		}

		public String getPingStatus() {
			return pingStatus;
		}

		public void setPingStatus(String pingStatus) {
			this.pingStatus = pingStatus;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}

		@Override
		public String toString() {
			return String.format(
					"ConnectionStatus{timestamp=%s, overallStatus='%s', mongoStatus='%s', database='%s', collections=%d}",
					timestamp, overallStatus, mongoStatus, database, collectionCount);
		}
	}
}