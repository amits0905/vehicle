package com.park_karo.vehicle.registration;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final UserRepository userRepository;

    // Dependency Injection of the UserRepository
    public RegistrationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Handles the core user registration process.
     * 
     * @param user The user object to register.
     * @return The saved User object.
     */
    public User registerUser(User user) {
        // --- 1. Synchronous (Critical) Task: Save User ---
        // This task runs on the main HTTP request thread and is blocking.
        User savedUser = userRepository.save(user);

        // --- 2. Asynchronous (Background) Task: Send Email ---
        // This method call is immediately handed off to a thread in the custom
        // Executor.
        // The main thread continues execution (returns savedUser) without waiting for
        // it to finish.
        sendConfirmationEmail(savedUser.getEmail());

        return savedUser;
    }

    /**
     * Executes the email sending simulation in a separate thread.
     * The task is assigned to the thread pool named "threadPoolTaskExecutor".
     * 
     * @param email The user's email address.
     */
    @Async("threadPoolTaskExecutor")
    public void sendConfirmationEmail(String email) {
        System.out.println("Starting email process for: " + email + " in thread: " + Thread.currentThread().getName());

        // --- SIMULATING A LONG I/O TASK (e.g., calling an external Email API) ---
        try {
            // Pause the thread for 3 seconds to simulate network latency/processing time
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // Best practice for handling InterruptedException
            Thread.currentThread().interrupt();
            System.err.println("Email process interrupted for " + email);
        }
        // -------------------------------------------------------------------------

        System.out.println("Email successfully sent to: " + email + " in thread: " + Thread.currentThread().getName());
    }

    /**
     * Example method to retrieve a user by email.
     * 
     * @param email The email address to search for.
     * @return The User object or null if not found.
     */
    public User getUserByEmail(String email) {
        // Assuming UserRepository provides a findByEmail method (synchronous for now)
        return userRepository.findByEmail(email).orElse(null);
    }
}