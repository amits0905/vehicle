package com.park_karo.vehicle.registration;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing User data in MongoDB.
 * It extends MongoRepository to inherit standard CRUD operations (like save,
 * findById, findAll).
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    /**
     * Custom query method: Spring Data automatically generates the implementation
     * to find a user document based on the email field.
     */
    // User findByEmail(String email);

    // The save() method is now automatically available and defined here.
    // Example: park_karomyresgiatrationuser save(park_karomyresgiatrationuser
    // user);
}