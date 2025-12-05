package com.park_karo.vehicle.dao;

import java.lang.reflect.ParameterizedType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.park_karo.vehicle.exception.CustomExceptions;

/**
 * Global BaseDAO with comprehensive error handling and logging All database
 * operations are wrapped with proper exception handling
 */
public abstract class GlobalBaseDAO<T> {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected final MongoTemplate mongoTemplate;
	protected final Class<T> entityClass;

	@SuppressWarnings("unchecked")
	public GlobalBaseDAO(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
		logger.info("Initialized GlobalBaseDAO for entity: {}", entityClass.getSimpleName());
	}

	// ============ CRUD OPERATIONS ============

	/**
	 * Save or update an entity with automatic timestamp
	 */
	public T save(T entity) {
		final String operation = "save";
		logger.debug("[{}] Saving entity: {}", operation, entity);

		try {
			// Auto-set timestamps if entity has these fields
			setTimestamps(entity, "createdAt", "updatedAt");

			T savedEntity = mongoTemplate.save(entity);
			logger.info("[{}] Successfully saved entity with ID: {}", operation, getId(savedEntity));
			return savedEntity;

		} catch (DataAccessException e) {
			logger.error("[{}] Database error saving entity: {}", operation, e.getMessage(), e);
			throw new RuntimeException("Failed to save entity to database", e);
		} catch (Exception e) {
			logger.error("[{}] Unexpected error saving entity: {}", operation, e.getMessage(), e);
			throw new RuntimeException("An unexpected error occurred while saving", e);
		}
	}

	/**
	 * Find entity by ID
	 */
	public Optional<T> findById(String id) {
		final String operation = "findById";
		logger.debug("[{}] Finding entity with ID: {}", operation, id);

		validateId(id, operation);

		try {
			T entity = mongoTemplate.findById(id, entityClass);

			if (entity == null) {
				logger.debug("[{}] Entity not found with ID: {}", operation, id);
				return Optional.empty();
			}

			logger.debug("[{}] Found entity with ID: {}", operation, id);
			return Optional.of(entity);

		} catch (DataAccessException e) {
			logger.error("[{}] Database error finding entity: {}", operation, e.getMessage(), e);
			throw new RuntimeException("Database error while finding entity", e);
		} catch (Exception e) {
			logger.error("[{}] Unexpected error finding entity: {}", operation, e.getMessage(), e);
			throw new RuntimeException("An unexpected error occurred while finding entity", e);
		}
	}

	/**
	 * Find entity by ID or throw exception
	 */
	public T findByIdOrThrow(String id) {
		return findById(id).orElseThrow(() -> {
			String message = String.format("%s with ID %s not found", entityClass.getSimpleName(), id);
			logger.warn("Entity not found: {}", message);
			return new CustomExceptions.ResourceNotFoundBusinessException(message);
		});
	}

	/**
	 * Find all entities
	 */
	public List<T> findAll() {
		final String operation = "findAll";
		logger.debug("[{}] Finding all entities", operation);

		try {
			List<T> entities = mongoTemplate.findAll(entityClass);
			logger.info("[{}] Found {} entities", operation, entities.size());
			return entities;

		} catch (DataAccessException e) {
			logger.error("[{}] Database error finding all entities: {}", operation, e.getMessage(), e);
			throw new RuntimeException("Database error while finding all entities", e);
		} catch (Exception e) {
			logger.error("[{}] Unexpected error finding all entities: {}", operation, e.getMessage(), e);
			throw new RuntimeException("An unexpected error occurred while finding all entities", e);
		}
	}

	/**
	 * Delete entity by ID
	 */
	public void deleteById(String id) {
		final String operation = "deleteById";
		logger.debug("[{}] Deleting entity with ID: {}", operation, id);

		validateId(id, operation);

		try {
			// Check if exists first
			if (!existsById(id)) {
				logger.warn("[{}] Entity not found for deletion: {}", operation, id);
				throw new CustomExceptions.ResourceNotFoundBusinessException(
						entityClass.getSimpleName() + " with ID " + id + " not found");
			}

			Query query = new Query(Criteria.where("_id").is(id));
			mongoTemplate.remove(query, entityClass);

			logger.info("[{}] Successfully deleted entity {}", operation, id);

		} catch (CustomExceptions.ResourceNotFoundBusinessException e) {
			throw e;
		} catch (DataAccessException e) {
			logger.error("[{}] Database error deleting entity: {}", operation, e.getMessage(), e);
			throw new RuntimeException("Database error while deleting entity", e);
		} catch (Exception e) {
			logger.error("[{}] Unexpected error deleting entity: {}", operation, e.getMessage(), e);
			throw new RuntimeException("An unexpected error occurred while deleting", e);
		}
	}

	/**
	 * Check if entity exists by ID
	 */
	public boolean existsById(String id) {
		final String operation = "existsById";

		if (id == null) {
			return false;
		}

		try {
			Query query = new Query(Criteria.where("_id").is(id));
			boolean exists = mongoTemplate.exists(query, entityClass);
			logger.debug("[{}] Entity {} exists: {}", operation, id, exists);
			return exists;

		} catch (DataAccessException e) {
			logger.error("[{}] Database error checking existence: {}", operation, e.getMessage(), e);
			throw new RuntimeException("Database error while checking existence", e);
		} catch (Exception e) {
			logger.error("[{}] Unexpected error checking existence: {}", operation, e.getMessage(), e);
			throw new RuntimeException("An unexpected error occurred while checking existence", e);
		}
	}

	/**
	 * Count all entities
	 */
	public long count() {
		final String operation = "count";

		try {
			long count = mongoTemplate.count(new Query(), entityClass);
			logger.debug("[{}] Total entities: {}", operation, count);
			return count;

		} catch (DataAccessException e) {
			logger.error("[{}] Database error counting entities: {}", operation, e.getMessage(), e);
			throw new RuntimeException("Database error while counting", e);
		} catch (Exception e) {
			logger.error("[{}] Unexpected error counting entities: {}", operation, e.getMessage(), e);
			throw new RuntimeException("An unexpected error occurred while counting", e);
		}
	}

	/**
	 * Find by field with exact match
	 */
	public List<T> findByField(String fieldName, Object value) {
		final String operation = "findByField";
		logger.debug("[{}] Finding by field {} = {}", operation, fieldName, value);

		if (fieldName == null || value == null) {
			throw new IllegalArgumentException("Field name and value cannot be null");
		}

		try {
			Query query = new Query(Criteria.where(fieldName).is(value));
			List<T> results = mongoTemplate.find(query, entityClass);

			logger.debug("[{}] Found {} entities with {} = {}", operation, results.size(), fieldName, value);
			return results;

		} catch (DataAccessException e) {
			logger.error("[{}] Database error finding by field: {}", operation, e.getMessage(), e);
			throw new RuntimeException("Database error while finding by field", e);
		} catch (Exception e) {
			logger.error("[{}] Unexpected error finding by field: {}", operation, e.getMessage(), e);
			throw new RuntimeException("An unexpected error occurred while finding by field", e);
		}
	}

	/**
	 * Find single entity by field
	 */
	public Optional<T> findOneByField(String fieldName, Object value) {
		final String operation = "findOneByField";
		logger.debug("[{}] Finding one by field {} = {}", operation, fieldName, value);

		try {
			List<T> results = findByField(fieldName, value);

			if (results.isEmpty()) {
				logger.debug("[{}] No entity found with {} = {}", operation, fieldName, value);
				return Optional.empty();
			}

			if (results.size() > 1) {
				logger.warn("[{}] Multiple entities found with {} = {}, returning first", operation, fieldName, value);
			}

			return Optional.of(results.get(0));

		} catch (Exception e) {
			logger.error("[{}] Error finding one by field: {}", operation, e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * Update specific fields of an entity
	 */
	public T update(String id, Map<String, Object> updates) {
		final String operation = "update";
		logger.debug("[{}] Updating entity {} with {} fields", operation, id, updates != null ? updates.size() : 0);

		validateId(id, operation);

		if (updates == null || updates.isEmpty()) {
			throw new IllegalArgumentException("Update fields cannot be null or empty");
		}

		try {
			Query query = new Query(Criteria.where("_id").is(id));
			Update update = new Update();

			// Add all updates
			updates.forEach(update::set);

			// Always update the timestamp
			update.set("updatedAt", LocalDateTime.now());

			// Execute update
			mongoTemplate.updateFirst(query, update, entityClass);

			// Return updated entity
			T updatedEntity = findByIdOrThrow(id);
			logger.info("[{}] Successfully updated entity {}", operation, id);
			return updatedEntity;

		} catch (DataAccessException e) {
			logger.error("[{}] Database error updating entity: {}", operation, e.getMessage(), e);
			throw new RuntimeException("Database error while updating entity", e);
		} catch (CustomExceptions.ResourceNotFoundBusinessException e) {
			throw e;
		} catch (Exception e) {
			logger.error("[{}] Unexpected error updating entity: {}", operation, e.getMessage(), e);
			throw new RuntimeException("An unexpected error occurred while updating", e);
		}
	}

	/**
	 * Update a single field
	 */
	public void updateField(String id, String field, Object value) {
		final String operation = "updateField";
		logger.debug("[{}] Updating field {} = {} for entity {}", operation, field, value, id);

		update(id, Map.of(field, value));
	}

	// ============ HELPER METHODS ============

	private void validateId(String id, String operation) {
		if (id == null || id.trim().isEmpty()) {
			String message = "ID cannot be null or empty";
			logger.error("[{}] Validation failed: {}", operation, message);
			throw new IllegalArgumentException(message);
		}
	}

	private String getId(T entity) {
		if (entity == null) {
			return "null";
		}

		try {
			// Try to get ID via reflection (assuming entity has getId() method)
			java.lang.reflect.Method getIdMethod = entity.getClass().getMethod("getId");
			Object id = getIdMethod.invoke(entity);
			return id != null ? id.toString() : "null";
		} catch (Exception e) {
			return entity.toString();
		}
	}

	private void setTimestamps(T entity, String createdAtField, String updatedAtField) {
		try {
			LocalDateTime now = LocalDateTime.now();

			// Set createdAt if it's null
			java.lang.reflect.Method getCreatedAt = getMethod(entity, "get" + capitalize(createdAtField));
			java.lang.reflect.Method setCreatedAt = getMethod(entity, "set" + capitalize(createdAtField));

			if (getCreatedAt != null && setCreatedAt != null) {
				Object currentCreatedAt = getCreatedAt.invoke(entity);
				if (currentCreatedAt == null) {
					setCreatedAt.invoke(entity, now);
				}
			}

			// Always set updatedAt
			java.lang.reflect.Method setUpdatedAt = getMethod(entity, "set" + capitalize(updatedAtField));
			if (setUpdatedAt != null) {
				setUpdatedAt.invoke(entity, now);
			}

		} catch (Exception e) {
			// Ignore - not all entities have timestamp fields
		}
	}

	private java.lang.reflect.Method getMethod(Object obj, String methodName) {
		try {
			return obj.getClass().getMethod(methodName);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	private String capitalize(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	// ============ UTILITY METHODS ============

	/**
	 * Get collection name
	 */
	public String getCollectionName() {
		return mongoTemplate.getCollectionName(entityClass);
	}

	/**
	 * Test database connection
	 */
	public String testConnection() {
		try {
			String dbName = mongoTemplate.getDb().getName();
			long count = count();
			return String.format("✅ Connected to MongoDB! Database: %s, Collection: %s, Documents: %d", dbName,
					getCollectionName(), count);
		} catch (Exception e) {
			return String.format("❌ MongoDB Connection Failed: %s", e.getMessage());
		}
	}
}