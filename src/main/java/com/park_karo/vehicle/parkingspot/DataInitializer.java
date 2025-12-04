package com.park_karo.vehicle.parkingspot;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final ParkingSpotRepository parkingSpotRepository;
    private final ResourceLoader resourceLoader;

    public DataInitializer(ParkingSpotRepository parkingSpotRepository, ResourceLoader resourceLoader) {
        this.parkingSpotRepository = parkingSpotRepository;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(String... args) throws Exception {
        // Fetch the current count first, regardless of whether it's zero or not
        long existingCount = parkingSpotRepository.count(); 
        
        if (existingCount == 0) {
            logger.info("No parking data found. Initializing mock data from JSON...");
            
            // Load and insert data
            try {
                // Accesses the file from the src/main/resources folder
                Resource resource = resourceLoader.getResource("classpath:mumbai_parking.json");
                
                // Check if the resource exists
                if (!resource.exists()) {
                    logger.error("❌ ERROR: 'mumbai_parking.json' not found in src/main/resources. Please run MumbaiParkingGenerator first.");
                    return;
                }
                
                try (InputStream inputStream = resource.getInputStream()) {
                    ObjectMapper mapper = new ObjectMapper();
                    
                    // Reads the JSON array into a List of ParkingSpot objects
                    // NOTE: This requires the ParkingSpot class to be defined.
                    List<ParkingSpot> spots = mapper.readValue(inputStream, new TypeReference<List<ParkingSpot>>() {});
                    
                    parkingSpotRepository.saveAll(spots);
                    
                    logger.info("✅ Successfully initialized {} parking spots into MongoDB!", spots.size());
                }
            } catch (IOException e) {
                logger.error("❌ Failed to load mock data: {}", e.getMessage(), e);
            }
        } else {
            // Log the specific count of existing spots, making the message informative
            logger.debug("Parking data already exists. Skipping initialization. Found {} existing spots.", existingCount);
        }
    }
}