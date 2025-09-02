package com.example.prakriti;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "CropDiagnosisApp.db";
    private static final int DATABASE_VERSION = 4; // Increased for TipOfTheDay table

    // User Table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USER_NAME = "user_name";
    public static final String COLUMN_USER_EMAIL = "user_email";
    public static final String COLUMN_USER_PASSWORD = "user_password";
    public static final String COLUMN_USER_GENDER = "user_gender";
    public static final String COLUMN_USER_REGION = "user_region";
    public static final String COLUMN_USER_AGE = "user_age"; // New age column

    // User Info Table (Profile Details)
    public static final String TABLE_USER_INFO = "user_info";
    public static final String COLUMN_INFO_ID = "info_id";
    public static final String COLUMN_FOREIGN_KEY_USER_ID = "user_id";
    public static final String COLUMN_FARM_LOCATION = "farm_location";
    public static final String COLUMN_CROP_TYPE = "crop_type";
    public static final String COLUMN_FARM_SIZE = "farm_size";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";

    // TipOfTheDay Table
    public static final String TABLE_TIPS = "TipOfTheDay";
    public static final String COLUMN_TIP_ID = "id";
    public static final String COLUMN_TIP_TEXT = "tip";

    // Create User Table SQL
    private static final String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
            COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USER_NAME + " TEXT NOT NULL, " +
            COLUMN_USER_EMAIL + " TEXT UNIQUE NOT NULL, " +
            COLUMN_USER_PASSWORD + " TEXT NOT NULL, " +
            COLUMN_USER_GENDER + " TEXT, " +
            COLUMN_USER_REGION + " TEXT, " +
            COLUMN_USER_AGE + " TEXT" + ")";

    // Create User Info Table SQL
    private static final String CREATE_USER_INFO_TABLE = "CREATE TABLE " + TABLE_USER_INFO + " (" +
            COLUMN_INFO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_FOREIGN_KEY_USER_ID + " INTEGER, " +
            COLUMN_FARM_LOCATION + " TEXT, " +
            COLUMN_CROP_TYPE + " TEXT, " +
            COLUMN_FARM_SIZE + " TEXT, " +
            COLUMN_PHONE_NUMBER + " TEXT, " +
            "FOREIGN KEY(" + COLUMN_FOREIGN_KEY_USER_ID + ") REFERENCES " +
            TABLE_USERS + "(" + COLUMN_USER_ID + ")" + ")";

    // Create TipOfTheDay Table SQL
    private static final String CREATE_TIP_TABLE = "CREATE TABLE " + TABLE_TIPS + " (" +
            COLUMN_TIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TIP_TEXT + " TEXT NOT NULL" + ")";

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_USER_INFO_TABLE);
        db.execSQL(CREATE_TIP_TABLE);

        // Insert default 10 tips
        insertDefaultTips(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_USER_GENDER + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_USER_REGION + " TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_USER_AGE + " TEXT");
        }
        if (oldVersion < 4) {
            db.execSQL(CREATE_TIP_TABLE);
            insertDefaultTips(db);
        }
    }

    // Insert default 10 tips
    private void insertDefaultTips(SQLiteDatabase db) {
        String[] tips = {
                "Water your crops early in the morning to reduce evaporation.",
                "Use organic compost to improve soil fertility.",
                "Rotate crops every season to maintain soil nutrients.",
                "Monitor for pests regularly to prevent infestations.",
                "Mulch around plants to retain soil moisture.",
                "Prune unnecessary leaves to help plants grow better.",
                "Apply fertilizer according to soil test recommendations.",
                "Protect young plants from strong winds and heavy rain.",
                "Harvest crops at the right maturity stage for best quality.",
                "Use drip irrigation to save water and deliver nutrients efficiently."
        };

        for (String tip : tips) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TIP_TEXT, tip);
            db.insert(TABLE_TIPS, null, values);
        }
    }

    // Get all tips
    public Cursor getAllTips() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TIPS,
                new String[]{COLUMN_TIP_ID, COLUMN_TIP_TEXT},
                null, null, null, null, null);
    }

    // Add new user with basic info
    public long addUser(String name, String email, String password, String gender, String region) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);
        values.put(COLUMN_USER_GENDER, gender);
        values.put(COLUMN_USER_REGION, region);
        values.put(COLUMN_USER_AGE, ""); // Default empty age

        long userId = db.insert(TABLE_USERS, null, values);

        // Create corresponding user_info entry
        if (userId != -1) {
            ContentValues infoValues = new ContentValues();
            infoValues.put(COLUMN_FOREIGN_KEY_USER_ID, userId);
            db.insert(TABLE_USER_INFO, null, infoValues);
        }

        return userId;
    }

    // Update user basic info including age
    public boolean updateUserBasicInfo(long userId, String name, String gender, String age, String region) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USER_GENDER, gender);
        values.put(COLUMN_USER_AGE, age);
        values.put(COLUMN_USER_REGION, region);

        int rowsAffected = db.update(
                TABLE_USERS,
                values,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        return rowsAffected > 0;
    }

    // Get user info by user ID
    public Cursor getUserInfo(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT u." + COLUMN_USER_NAME + ", u." + COLUMN_USER_EMAIL +
                ", u." + COLUMN_USER_GENDER + ", u." + COLUMN_USER_AGE +
                ", u." + COLUMN_USER_REGION +
                ", i." + COLUMN_FARM_LOCATION + ", i." + COLUMN_CROP_TYPE +
                ", i." + COLUMN_FARM_SIZE + ", i." + COLUMN_PHONE_NUMBER +
                " FROM " + TABLE_USERS + " u LEFT JOIN " + TABLE_USER_INFO +
                " i ON u." + COLUMN_USER_ID + " = i." + COLUMN_FOREIGN_KEY_USER_ID +
                " WHERE u." + COLUMN_USER_ID + " = ?";

        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    // Check if user exists and return user ID
    public long authenticateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USER_EMAIL + "=? AND " + COLUMN_USER_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null);

        long userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
        }
        cursor.close();
        return userId;
    }

    // Check if user exists
    public boolean checkUser(String email, String password) {
        return authenticateUser(email, password) != -1;
    }

    public long getUserId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USER_EMAIL + "=?",
                new String[]{email},
                null, null, null);

        long userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
        }
        cursor.close();
        return userId;
    }

    // Check if email already exists
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USER_EMAIL + "=?",
                new String[]{email},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}
