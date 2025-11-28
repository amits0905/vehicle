package com.park_karo.vehicle.parkingspot.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "mumbai_parking_db")
public class ParkingSpot {

    @Id
    private String id; // Unique ID for the parking spot
    private String name;
    private double latitude;
    private double longitude;
    private int availableSpaces;
    private double hourlyRate;
    private String vehicleType; 

    public ParkingSpot() {
    }

    // Constructor for easy initialization (optional, but helpful)
    public ParkingSpot(String id, String name, double latitude, double longitude, int availableSpaces, double hourlyRate, String vehicleType) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.availableSpaces = availableSpaces;
        this.hourlyRate = hourlyRate;
        this.vehicleType = vehicleType;
    }

    // Getters and Setters (Essential for Spring Data MongoDB)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getAvailableSpaces() {
        return availableSpaces;
    }

    public void setAvailableSpaces(int availableSpaces) {
        this.availableSpaces = availableSpaces;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
}