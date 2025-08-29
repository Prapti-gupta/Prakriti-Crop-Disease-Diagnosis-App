package com.example.prakriti;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for managing crop data operations
 * Provides high-level methods for retrieving crop information
 */
public class CropDataManager {

    private DatabaseHelper dbHelper;
    private Context context;

    public CropDataManager(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * Get healthy crop information for display in Crops activity
     */
    public DatabaseHelper.CropInfo getHealthyCropDetails(String cropName) {
        return dbHelper.getHealthyCropInfo(cropName);
    }

    /**
     * Get disease information for display in CameraDescription activity
     */
    public DatabaseHelper.CropInfo getDiseaseDetails(String prediction) {
        return dbHelper.getDiseaseInfo(prediction);
    }

    /**
     * Get all available crops (healthy ones only)
     */
    public List<String> getAvailableCrops() {
        List<String> crops = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT DISTINCT crop FROM crops WHERE condition_disease = 'Healthy' ORDER BY crop";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                crops.add(cursor.getString(0));
            }
            cursor.close();
        }

        db.close();
        return crops;
    }

    /**
     * Get all diseases for a specific crop
     */
    public List<DatabaseHelper.CropInfo> getAllCropConditions(String cropName) {
        List<DatabaseHelper.CropInfo> conditions = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = "crop = ?";
        String[] selectionArgs = {cropName};

        Cursor cursor = db.query("crops", null, selection, selectionArgs, null, null, "condition_disease");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                DatabaseHelper.CropInfo info = new DatabaseHelper.CropInfo(
                        cursor.getString(cursor.getColumnIndexOrThrow("crop")),
                        cursor.getString(cursor.getColumnIndexOrThrow("condition_disease")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("treatment_care_tips"))
                );
                conditions.add(info);
            }
            cursor.close();
        }

        db.close();
        return conditions;
    }

    /**
     * Search for diseases by partial name match
     */
    public List<DatabaseHelper.CropInfo> searchDiseases(String searchTerm) {
        List<DatabaseHelper.CropInfo> results = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = "condition_disease LIKE ? OR description LIKE ?";
        String[] selectionArgs = {"%" + searchTerm + "%", "%" + searchTerm + "%"};

        Cursor cursor = db.query("crops", null, selection, selectionArgs, null, null, "crop, condition_disease");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                DatabaseHelper.CropInfo info = new DatabaseHelper.CropInfo(
                        cursor.getString(cursor.getColumnIndexOrThrow("crop")),
                        cursor.getString(cursor.getColumnIndexOrThrow("condition_disease")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("treatment_care_tips"))
                );
                results.add(info);
            }
            cursor.close();
        }

        db.close();
        return results;
    }

    /**
     * Check if a crop exists in the database
     */
    public boolean cropExists(String cropName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = "crop = ?";
        String[] selectionArgs = {cropName};

        Cursor cursor = db.query("crops", new String[]{"crop"}, selection, selectionArgs, null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;

        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return exists;
    }

    /**
     * Get crop information with fuzzy matching for better prediction handling
     */
    public DatabaseHelper.CropInfo getBestMatchForPrediction(String prediction) {
        // First try exact match
        DatabaseHelper.CropInfo exactMatch = dbHelper.getDiseaseInfo(prediction);
        if (exactMatch != null) {
            return exactMatch;
        }

        // Try fuzzy matching
        List<DatabaseHelper.CropInfo> searchResults = searchDiseases(prediction);
        if (!searchResults.isEmpty()) {
            return searchResults.get(0); // Return the first match
        }

        // Try matching individual words
        String[] words = prediction.toLowerCase().split("[\\s_-]+");
        for (String word : words) {
            if (word.length() > 3) { // Only search for words longer than 3 characters
                List<DatabaseHelper.CropInfo> wordResults = searchDiseases(word);
                if (!wordResults.isEmpty()) {
                    return wordResults.get(0);
                }
            }
        }

        return null; // No match found
    }

    /**
     * Get statistics about the database
     */
    public DatabaseStats getDatabaseStats() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        DatabaseStats stats = new DatabaseStats();

        // Count total entries
        Cursor totalCursor = db.rawQuery("SELECT COUNT(*) FROM crops", null);
        if (totalCursor != null && totalCursor.moveToFirst()) {
            stats.totalEntries = totalCursor.getInt(0);
            totalCursor.close();
        }

        // Count unique crops
        Cursor cropsCursor = db.rawQuery("SELECT COUNT(DISTINCT crop) FROM crops", null);
        if (cropsCursor != null && cropsCursor.moveToFirst()) {
            stats.uniqueCrops = cropsCursor.getInt(0);
            cropsCursor.close();
        }

        // Count healthy entries
        Cursor healthyCursor = db.rawQuery("SELECT COUNT(*) FROM crops WHERE condition_disease = 'Healthy'", null);
        if (healthyCursor != null && healthyCursor.moveToFirst()) {
            stats.healthyEntries = healthyCursor.getInt(0);
            healthyCursor.close();
        }

        // Count disease entries
        stats.diseaseEntries = stats.totalEntries - stats.healthyEntries;

        db.close();
        return stats;
    }

    /**
     * Close the database helper
     */
    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    /**
     * Inner class to hold database statistics
     */
    public static class DatabaseStats {
        public int totalEntries;
        public int uniqueCrops;
        public int healthyEntries;
        public int diseaseEntries;

        @Override
        public String toString() {
            return "Database Stats:\n" +
                    "Total Entries: " + totalEntries + "\n" +
                    "Unique Crops: " + uniqueCrops + "\n" +
                    "Healthy Entries: " + healthyEntries + "\n" +
                    "Disease Entries: " + diseaseEntries;
        }
    }
}