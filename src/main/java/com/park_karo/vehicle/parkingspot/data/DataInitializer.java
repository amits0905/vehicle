package com.park_karo.vehicle.parkingspot.data;

import java.io.IOException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.park_karo.vehicle.parkingspot.model.ParkingSpot;
import com.park_karo.vehicle.parkingspot.repository.ParkingSpotRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ParkingSpotRepository parkingSpotRepository;
    private final ResourceLoader resourceLoader;

    public DataInitializer(ParkingSpotRepository parkingSpotRepository, ResourceLoader resourceLoader) {
        this.parkingSpotRepository = parkingSpotRepository;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(String... args) throws Exception {
        if (parkingSpotRepository.count() == 0) {
            System.out.println("No parking data found. Initializing mock data from JSON...");
            
            // Load and insert data
            try {
                // Accesses the file from the src/main/resources folder
                Resource resource = resourceLoader.getResource("classpath:mumbai_parking.json");
                
                // Check if the resource exists
                if (!resource.exists()) {
                    System.err.println("❌ ERROR: 'mumbai_parking.json' not found in src/main/resources. Please run MumbaiParkingGenerator first.");
                    return;
                }
                
                try (InputStream inputStream = resource.getInputStream()) {
                    ObjectMapper mapper = new ObjectMapper();
                    
                    // Reads the JSON array into a List of ParkingSpot objects
                    List<ParkingSpot> spots = mapper.readValue(inputStream, new TypeReference<List<ParkingSpot>>() {});
                    
                    parkingSpotRepository.saveAll(spots);
                    
                    System.out.println("✅ Successfully initialized " + spots.size() + " parking spots into MongoDB!");
                }
            } catch (IOException e) {
                System.err.println("❌ Failed to load mock data: " + e.getMessage());
            }
        } else {
            // This is the message you saw in your logs!
            System.out.println("Parking data already exists. Skipping initialization.");
        }
    }
}