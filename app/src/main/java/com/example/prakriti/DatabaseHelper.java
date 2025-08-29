package com.example.prakriti;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.ContentValues;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CropDatabase.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_CROPS = "crops";

    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CROP = "crop";
    private static final String COLUMN_CONDITION = "condition_disease";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_TREATMENT = "treatment_care_tips";

    // Create table SQL statement
    private static final String CREATE_TABLE_CROPS =
            "CREATE TABLE " + TABLE_CROPS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_CROP + " TEXT NOT NULL," +
                    COLUMN_CONDITION + " TEXT NOT NULL," +
                    COLUMN_DESCRIPTION + " TEXT NOT NULL," +
                    COLUMN_TREATMENT + " TEXT NOT NULL" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CROPS);
        populateInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CROPS);
        onCreate(db);
    }

    private void populateInitialData(SQLiteDatabase db) {
        // Insert all the crop data from your CSV
        insertCropData(db, "Apple", "Apple_scab", "Fungal disease causing scabby lesions on leaves and fruits.", "Use resistant varieties, prune trees, apply fungicides.");
        insertCropData(db, "Apple", "Black_rot", "Fungal disease causing fruit rot and leaf spots.", "Remove infected parts, apply fungicides, proper sanitation.");
        insertCropData(db, "Apple", "Cedar_apple_rust", "Fungal disease causing bright orange spots on leaves.", "Remove nearby junipers, apply fungicides, resistant varieties.");
        insertCropData(db, "Apple", "Healthy", "Tree without visible disease.", "Prune regularly, fertilize, ensure proper irrigation.");

        insertCropData(db, "Cherry", "Healthy", "Tree growing without disease.", "Water well, mulch, prune for airflow.");
        insertCropData(db, "Cherry", "Powdery_mildew", "Fungal disease causing white powder on leaves.", "Apply sulfur fungicides, prune infected leaves, ensure airflow.");

        insertCropData(db, "Coffee", "Healthy", "Plant with no disease symptoms.", "Provide shade, irrigate properly, prune for airflow.");
        insertCropData(db, "Coffee", "Miner", "Leaf miner insect creating tunnels in leaves.", "Use neem oil, remove affected leaves, biological control.");
        insertCropData(db, "Coffee", "Rust", "Fungal disease causing orange powdery spots on leaves.", "Use resistant varieties, copper-based fungicides, prune trees.");

        insertCropData(db, "Corn", "Cercospora_leaf_spot Gray_leaf_spot", "Fungal disease causing gray rectangular lesions.", "Rotate crops, apply fungicides, resistant varieties.");
        insertCropData(db, "Corn", "Common_rust", "Fungal disease producing reddish-brown pustules.", "Use resistant hybrids, fungicide sprays, rotate crops.");
        insertCropData(db, "Corn", "Healthy", "Corn plants without visible damage.", "Balanced fertilization, irrigation, weed control.");
        insertCropData(db, "Corn", "Northern_Leaf_Blight", "Fungal disease causing cigar-shaped lesions.", "Resistant hybrids, fungicide sprays, crop rotation.");

        insertCropData(db, "Cotton", "Bacterial_blight", "Bacterial disease with angular leaf spots.", "Use resistant seeds, avoid overhead irrigation, crop rotation.");
        insertCropData(db, "Cotton", "Aphids", "Small insects sucking sap from plants.", "Introduce ladybugs, neem oil spray, insecticidal soap.");
        insertCropData(db, "Cotton", "Army_worm", "Caterpillars feeding on leaves.", "Handpick larvae, apply BT sprays, pheromone traps.");
        insertCropData(db, "Cotton", "Healthy", "Plant without disease.", "Use certified seeds, timely irrigation, balanced fertilization.");
        insertCropData(db, "Cotton", "Powdery_mildew", "White powdery growth on leaves.", "Apply sulfur fungicides, remove infected leaves.");
        insertCropData(db, "Cotton", "Target_spot", "Fungal disease causing concentric ring spots.", "Crop rotation, fungicides, field sanitation.");

        insertCropData(db, "Grape", "Black_rot", "Fungal disease causing shriveled black berries.", "Remove infected fruit, fungicide application, prune vines.");
        insertCropData(db, "Grape", "Esca_(Black_Measles)", "Trunk disease causing leaf striping and berry shriveling.", "Remove infected vines, avoid stress, fungicide sprays.");
        insertCropData(db, "Grape", "Healthy", "Vines growing without disease.", "Prune annually, maintain airflow, regular irrigation.");
        insertCropData(db, "Grape", "Leaf_blight_(Isariopsis_Leaf_Spot)", "Fungal disease causing necrotic leaf spots.", "Remove infected leaves, apply fungicides, crop rotation.");

        insertCropData(db, "Jute", "Cescospora_leaf_spot", "Leaf spot disease with brown circular lesions.", "Remove infected leaves, fungicide spray, crop rotation.");
        insertCropData(db, "Jute", "Golden_mosaic", "Viral disease causing yellow mosaic on leaves.", "Use resistant varieties, control whiteflies, remove infected plants.");
        insertCropData(db, "Jute", "Healthy", "Healthy jute plant with no disease.", "Adequate irrigation, weed management, fertilizer application.");

        insertCropData(db, "Orange", "Haunglongbing_(Citrus_greening)", "Serious bacterial disease causing yellow shoots and bitter fruits.", "Remove infected trees, control psyllid vector, resistant rootstocks.");

        insertCropData(db, "Pepper_bell", "Bacterial_spot", "Bacterial disease causing dark water-soaked spots.", "Use copper sprays, resistant seeds, crop rotation.");
        insertCropData(db, "Pepper_bell", "Healthy", "Healthy pepper plants.", "Drip irrigation, fertilizer balance, pest monitoring.");

        insertCropData(db, "Potato", "Early_blight", "Fungal disease causing concentric brown leaf spots.", "Remove infected leaves, fungicide sprays, rotate crops.");
        insertCropData(db, "Potato", "Healthy", "Potato plants without disease.", "Proper irrigation, resistant varieties, balanced fertilization.");
        insertCropData(db, "Potato", "Late_blight", "Severe fungal disease causing blackened leaves and tubers.", "Fungicide sprays, resistant varieties, remove infected plants.");

        insertCropData(db, "Rice", "Brown_Spot", "Fungal disease causing brown leaf lesions.", "Balanced fertilization, fungicide sprays, resistant varieties.");
        insertCropData(db, "Rice", "Healthy", "Rice growing without disease.", "Timely irrigation, weed control, proper spacing.");
        insertCropData(db, "Rice", "Leaf_Blast", "Fungal disease causing spindle-shaped lesions.", "Resistant varieties, fungicide sprays, avoid excess nitrogen.");
        insertCropData(db, "Rice", "Neck_Blast", "Severe blast disease at panicle base.", "Resistant varieties, fungicides, field sanitation.");

        insertCropData(db, "Strawberry", "Healthy", "Healthy strawberry plants.", "Mulching, irrigation management, remove old leaves.");
        insertCropData(db, "Strawberry", "Leaf_scorch", "Fungal disease causing burnt-looking leaves.", "Remove infected leaves, fungicide sprays, crop rotation.");

        insertCropData(db, "Sugarcane", "Bacterial_Blight", "Bacterial disease causing leaf streaks.", "Use healthy setts, resistant varieties, crop rotation.");
        insertCropData(db, "Sugarcane", "Healthy", "Healthy sugarcane plants.", "Irrigation scheduling, weed management, soil fertility improvement.");
        insertCropData(db, "Sugarcane", "Red_Rot", "Fungal disease causing red discoloration inside stalks.", "Resistant varieties, remove infected clumps, crop rotation.");

        insertCropData(db, "Tomato", "Bacterial_spot", "Bacterial disease with small water-soaked lesions.", "Copper sprays, crop rotation, resistant seeds.");
        insertCropData(db, "Tomato", "Early_blight", "Fungal disease causing concentric brown spots.", "Remove infected leaves, fungicides, crop rotation.");
        insertCropData(db, "Tomato", "Healthy", "Tomato plant without disease.", "Proper staking, irrigation, pest monitoring.");
        insertCropData(db, "Tomato", "Late_blight", "Deadly fungal disease causing leaf and fruit rot.", "Fungicide sprays, resistant varieties, remove infected plants.");
        insertCropData(db, "Tomato", "Leaf_Mold", "Fungal disease causing yellow patches and mold under leaves.", "Improve airflow, resistant varieties, fungicide sprays.");
        insertCropData(db, "Tomato", "Septoria_leaf_spot", "Fungal disease causing small circular leaf spots.", "Remove infected leaves, fungicide sprays, crop rotation.");
        insertCropData(db, "Tomato", "Spider_mites_Two-spotted_spider_mite", "Insect pests causing stippling and webbing.", "Neem oil, miticides, encourage predatory mites.");
        insertCropData(db, "Tomato", "Target_Spot", "Fungal disease causing concentric spots on leaves.", "Fungicide sprays, crop rotation, remove infected debris.");
        insertCropData(db, "Tomato", "Tomato_Yellow_Leaf_Curl_Virus", "Viral disease causing yellow curled leaves.", "Use resistant varieties, control whiteflies, remove infected plants.");
        insertCropData(db, "Tomato", "Tomato_mosaic_virus", "Viral disease causing mottled, distorted leaves.", "Use virus-free seeds, disinfect tools, resistant varieties.");

        insertCropData(db, "Wheat", "Brown_Rust", "Fungal disease with brown pustules on leaves.", "Resistant varieties, fungicide sprays, crop rotation.");
        insertCropData(db, "Wheat", "Healthy", "Wheat growing without disease.", "Balanced fertilizer, irrigation, pest monitoring.");
        insertCropData(db, "Wheat", "Yellow_Rust", "Fungal disease producing yellow stripe-like pustules.", "Resistant varieties, fungicide sprays, crop monitoring.");
    }

    private void insertCropData(SQLiteDatabase db, String crop, String condition, String description, String treatment) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_CROP, crop);
        values.put(COLUMN_CONDITION, condition);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_TREATMENT, treatment);
        db.insert(TABLE_CROPS, null, values);
    }

    // Method to get healthy crop information by crop name (for Crops.java)
    public CropInfo getHealthyCropInfo(String cropName) {
        SQLiteDatabase db = this.getReadableDatabase();
        CropInfo cropInfo = null;

        String selection = COLUMN_CROP + " = ? AND " + COLUMN_CONDITION + " = ?";
        String[] selectionArgs = {cropName, "Healthy"};

        Cursor cursor = db.query(TABLE_CROPS, null, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            cropInfo = new CropInfo(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CROP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONDITION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TREATMENT))
            );
            cursor.close();
        }

        db.close();
        return cropInfo;
    }

    // Alternative method name for backward compatibility
    public Cursor getCropDetails(String cropName) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_CROP + " = ? AND " + COLUMN_CONDITION + " = ?";
        String[] selectionArgs = {cropName, "Healthy"};

        return db.query(TABLE_CROPS, null, selection, selectionArgs, null, null, null);
    }

    // Method to get disease information by condition/disease name (for CameraDescription.java)
    public CropInfo getDiseaseInfo(String conditionName) {
        SQLiteDatabase db = this.getReadableDatabase();
        CropInfo cropInfo = null;

        // Handle various formats of disease names
        String normalizedCondition = normalizeConditionName(conditionName);

        String selection = COLUMN_CONDITION + " = ? OR " + COLUMN_CONDITION + " LIKE ?";
        String[] selectionArgs = {normalizedCondition, "%" + normalizedCondition + "%"};

        Cursor cursor = db.query(TABLE_CROPS, null, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            cropInfo = new CropInfo(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CROP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONDITION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TREATMENT))
            );
            cursor.close();
        }

        db.close();
        return cropInfo;
    }

    // Helper method to normalize condition names for better matching
    private String normalizeConditionName(String conditionName) {
        if (conditionName == null) return "";

        // Replace common variations
        return conditionName
                .replace(" ", "_")
                .replace("-", "_")
                .replace("__", "_")
                .toLowerCase()
                .replaceAll("^_+|_+$", ""); // Remove leading/trailing underscores
    }

    // Inner class to hold crop information
    public static class CropInfo {
        public String cropName;
        public String condition;
        public String description;
        public String treatment;

        public CropInfo(String cropName, String condition, String description, String treatment) {
            this.cropName = cropName;
            this.condition = condition;
            this.description = description;
            this.treatment = treatment;
        }
    }
}