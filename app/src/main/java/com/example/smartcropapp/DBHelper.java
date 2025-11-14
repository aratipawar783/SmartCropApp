package com.example.smartcropapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DBNAME = "SmartCrop.db";

    public DBHelper(Context context) {
        super(context, DBNAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "email TEXT, " +
                "phone TEXT, " +
                "password TEXT)");


        db.execSQL("CREATE TABLE IF NOT EXISTS user_profile (" +
                "id INTEGER PRIMARY KEY," +
                "name TEXT," +
                "phone TEXT," +
                "email TEXT," +
                "address TEXT," +
                "aadhar TEXT," +
                "dob TEXT," +
                "photo TEXT" + // store image URI as TEXT
                ")");
        ContentValues cv = new ContentValues();
        cv.put("id", 1);
        cv.put("name", "");
        cv.put("phone", "");
        cv.put("email", "");
        cv.put("address", "");
        cv.put("aadhar", "");
        cv.put("dob", "");
        cv.put("photo", "");
        db.insertWithOnConflict("user_profile", null, cv, SQLiteDatabase.CONFLICT_IGNORE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS user_profile");
        onCreate(db);
    }
    public boolean saveUserProfile(String name, String phone, String email, String address,
                                   String aadhar, String dob, String photoUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", 1);
        values.put("name", name);
        values.put("phone", phone);
        values.put("email", email);
        values.put("address", address);
        values.put("aadhar", aadhar);
        values.put("dob", dob);
        values.put("photo", photoUri == null ? "" : photoUri);

        long res = db.insertWithOnConflict("user_profile", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return res != -1;
    }

    /**
     * Update only the photo column (keeps other fields untouched)
     */
    public boolean updateProfilePhoto(String photoUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("photo", photoUri == null ? "" : photoUri);
        int rows = db.update("user_profile", values, "id=?", new String[]{"1"});
        return rows > 0;
    }

    /**
     * Get profile row (cursor) for reading
     */
    public Cursor getUserProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM user_profile WHERE id=1 LIMIT 1", null);
    }



    // ✅ Insert new user
    public boolean insertUser(String username, String email, String phone, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("email", email);
        values.put("phone", phone);
        values.put("password", password);
        long result = db.insert("users", null, values);
        db.close();
        return result != -1;
    }

    // ✅ Check login (username + email/phone + password)
    public boolean checkUserLogin(String username, String emailOrPhone, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE username=? AND (email=? OR phone=?) AND password=?",
                new String[]{username, emailOrPhone, emailOrPhone, password}
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // ✅ Check if user exists (for signup & forgot password)
    public boolean checkUserExistsByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE email=? ",
                new String[]{email}
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    public boolean checkUserExistsByPhone(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE phone = ?", new String[]{phone});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }



    // ✅ Update password by Email or Phone
    public boolean updatePassword(String emailOrPhone, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);

        int result = db.update("users", values, "email=? OR phone=?", new String[]{emailOrPhone, emailOrPhone});
        return result > 0;
    }

    // ✅ Update password by Phone only (for Firebase OTP)
    public boolean updatePasswordByPhone(String phone, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);

        int result = db.update("users", values, "phone=?", new String[]{phone});
        return result > 0;
    }
    // ✅ Insert feedback into database


    // ✅ Get all feedbacks from database


}
