// package com.park_karo.vehicle.utils;

// import java.io.FileWriter;
// import java.io.IOException;
// import java.util.Random;

// // NOTE: You must ensure the 'org.json' dependency is in your project's classpath (e.g., pom.xml or build.gradle)
// import org.json.JSONArray;
// import org.json.JSONObject;

// public class MumbaiParkingGenerator {

//     // --- POST OFFICE–WISE AREA LIST (Used for generating realistic location names) ---
//     private static final String[] AREAS = {
//         "Mumbai GPO", "Colaba PO", "Cuffe Parade PO", "Fort PO", "Ballard Estate PO",
//         "Marine Lines PO", "Girgaon PO", "Grant Road PO", "Malabar Hill PO",
//         "Tardeo PO", "Byculla PO", "Mazgaon PO", "Dockyard Road PO",
//         "Worli PO", "Prabhadevi PO", "Dadar HO", "Mahim PO",
//         "Sewri PO", "Parel PO", "Lalbaug PO", "Chinchpokli PO",
//         "Matunga PO", "Sion PO", "Wadala PO", "Antop Hill PO",
//         "Bandra West PO", "Bandra East PO", "Khar West PO", "Khar East PO",
//         "Santacruz West PO", "Santacruz East PO", "Juhu PO",
//         "Vile Parle West PO", "Vile Parle East PO",
//         "Andheri West PO", "Andheri East PO", "Versova PO",
//         "Jogeshwari PO", "Goregaon West PO", "Goregaon East PO",
//         "Malad West PO", "Malad East PO", "Kandivali West PO",
//         "Kandivali East PO", "Borivali West PO", "Borivali East PO",
//         "Dahisar West PO", "Dahisar East PO",
//         "Chembur PO", "Chembur Colony PO", "Mahul PO",
//         "Govandi PO", "Mankhurd PO", "Deonar PO",
//         "Ghatkopar West PO", "Ghatkopar East PO", "Pant Nagar PO",
//         "Vidyavihar PO", "Vikhroli West PO", "Vikhroli East PO",
//         "Kanjurmarg PO", "Bhandup West PO", "Bhandup East PO",
//         "Mulund West PO", "Mulund East PO", "Nahur PO",
//         "Kurla West PO", "Kurla East PO", "Nehru Nagar PO", "Bail Bazar PO",
//         "Powai PO", "Marol PO", "Saki Naka PO", "Chandivali PO", "MIDC Andheri PO",
//         "Sewree PO", "Reay Road PO", "Cotton Green PO", "Wadala Road PO",
//         "Vashi PO", "Sanpada PO", "Juinagar PO", "Nerul PO",
//         "Seawoods PO", "CBD Belapur PO", "Kharghar PO",
//         "Kalamboli PO", "Kamothe PO", "Panvel HO",
//         "Airoli PO", "Rabale PO", "Ghansoli PO", "Koparkhairane PO",
//         "Thane HO", "Thane East PO", "Thane West PO", "Balkum PO",
//         "Majiwada PO", "Wagle Estate PO", "Louis Wadi PO",
//         "Kopri PO", "Manpada PO",
//         "Mira Road PO", "Bhayandar West PO", "Bhayandar East PO",
//         "Vasai Road PO", "Naigaon PO", "Virar PO", "Nalasopara PO",
//         "Charkop PO", "Kharodi PO", "Marve PO", "Aksa PO",
//         "Shimpoli PO", "Kandarpada PO", "Eksar PO",
//         "Charkop Sector 8 PO", "Chakala PO", "Sher-E-Punjab PO",
//         "DN Nagar PO", "Four Bungalows PO", "Seven Bungalows PO",
//         "Sion East PO", "Wadala East PO", "Kings Circle PO",
//         "Sewri East PO", "Lalbaug Market PO",
//         "Peddar Road PO", "Breach Candy PO",
//         "Opera House PO", "Bombay Hospital PO", "Charni Road PO",
//         "Princess Dock PO", "Carnac Bunder PO", "Mohammed Ali Road PO",
//         "Crawford Market PO", "Null Bazaar PO",
//         "Turbhe PO", "Ghansoli Sector 3 PO", "Nerul East PO",
//         "Nerul West PO", "Belapur Node PO",
//         "Kalwa PO", "Mumbra PO", "Kausa PO",
//         "Vasai East PO", "Bandra Kurla Complex PO", "Jijamata Udyan PO",
//         "Walkeshwar PO", "Sion Koliwada PO",
//         "Worli Police Camp PO", "BKC Income Tax Colony PO"
//     };

//     /**
//      * Generates the mock parking spot data and writes it to a JSON file.
//      */
//     public static void generateData() {
//         // JSON builder and Randomizer
//         JSONArray jsonArray = new JSONArray();
//         Random rand = new Random();

//         // --- GENERATE 1000 ENTRIES ---
//         for (int i = 1; i <= 1000; i++) {

//             String id = String.format("PS-MUM-%04d", i);

//             // Select a PO area randomly
//             String area = AREAS[rand.nextInt(AREAS.length)];

//             // Random but Mumbai-like coordinates (approx 18.9°N to 19.3°N and 72.8°E to 73.1°E)
//             double latitude = 18.89 + (19.30 - 18.89) * rand.nextDouble();
//             double longitude = 72.80 + (73.10 - 72.80) * rand.nextDouble();

//             int spaces = rand.nextInt(121);          // 0–120
//             double rate = 40 + (250 - 40) * rand.nextDouble(); // ₹40–₹250

//             JSONObject obj = new JSONObject();
//             obj.put("id", id);
//             obj.put("name", area.replace(" PO", "") + " Parking Zone " + (rand.nextInt(9) + 1));
//             // Rounding to 6 decimal places
//             obj.put("latitude", Math.round(latitude * 1_000_000d) / 1_000_000d);
//             obj.put("longitude", Math.round(longitude * 1_000_000d) / 1_000_000d);
//             obj.put("availableSpaces", spaces);
//             // Rounding to 2 decimal places
//             obj.put("hourlyRate", Math.round(rate * 100d) / 100d);
//             obj.put("vehicleType", rand.nextInt(2) == 0 ? "CAR" : "BIKE");

//             jsonArray.put(obj);
//         }

//         // --- WRITE JSON FILE ---
//         // Construct the relative path to the resources folder
//         String filePath = "src/main/resources/mumbai_parking.json";
        
//         try (FileWriter fw = new FileWriter(filePath)) {
//             fw.write(jsonArray.toString(2)); // pretty-print
//             System.out.println("✅ Generated " + filePath + " with 1000 mock entries!");
//         } catch (IOException e) {
//             System.err.println("❌ Error writing file. Ensure the 'src/main/resources' directory exists.");
//             e.getMessage();
//         }
//     }

//     /**
//      * Main method to execute the data generation manually.
//      * Run this class specifically to create the JSON file.
//      */
//     public static void main(String[] args) {
//         System.out.println("Starting Mumbai Parking Data Generation...");
//         generateData();
//     }
// }