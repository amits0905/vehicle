package com.park_karo.vehicle.dto;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VehicleDTO {

	@NotBlank(message = "Vehicle ID is required")
	private String vehicleId;

	@NotBlank(message = "Vehicle type is required")
	private String type; // car, bike, truck

	@NotBlank(message = "Registration number is required")
	@Size(min = 3, max = 20, message = "Registration number must be between 3 and 20 characters")
	private String registrationNumber;

	@NotBlank(message = "Brand is required")
	private String brand;

	@NotBlank(message = "Model is required")
	private String model;

	private String color;

	@NotNull(message = "Year is required")
	private Integer year;

	private Map<String, Object> additionalInfo = new HashMap<>();

	// Getters and Setters
	public String getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(String vehicleId) {
		this.vehicleId = vehicleId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Map<String, Object> getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(Map<String, Object> additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	// Convert to Map for MongoDB storage
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("vehicle_id", vehicleId);
		map.put("type", type);
		map.put("registration_number", registrationNumber);
		map.put("brand", brand);
		map.put("model", model);
		map.put("color", color);
		map.put("year", year);
		map.put("additional_info", additionalInfo);
		map.put("created_at", java.time.Instant.now().toString());
		map.put("updated_at", java.time.Instant.now().toString());
		return map;
	}
}