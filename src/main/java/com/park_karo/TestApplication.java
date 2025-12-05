package com.park_karo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

class TestMongo {
	public static void main(String[] args) {
		String uri = "mongodb+srv://testapi0905_db_user:jgHPXKi8eZeee057@testandtry.99i2u3s.mongodb.net/park_karo_db?retryWrites=true&w=majority";

		System.out.println("Testing MongoDB connection...");
		System.out.println("URI: " + uri.replace("jgHPXKi8eZeee057", "*****"));

		try (MongoClient mongoClient = MongoClients.create(uri)) {
			System.out.println("✅ SUCCESS! Connected to MongoDB");
			System.out.println("Database: " + mongoClient.getDatabase("park_karo_db").getName());
		} catch (Exception e) {
			System.out.println("❌ FAILED: " + e.getMessage());
		}
	}
}