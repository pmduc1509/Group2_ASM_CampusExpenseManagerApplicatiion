package com.example.group2_asm_campusexpensemanagerapplicatiion.Database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.group2_asm_campusexpensemanagerapplicatiion.Models.User;

public class UserDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "group2_asm2_database";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "users";
    private static final String ID_COL = "id";
    private static final String USERNAME_COL = "username";
    private static final String PASSWORD_COL = "password";
    private static final String EMAIL_COL = "email";
    private static final String PHONE_COL = "phone";

    public UserDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USERNAME_COL + " TEXT NOT NULL UNIQUE, " +
                PASSWORD_COL + " TEXT NOT NULL, " +
                EMAIL_COL + " TEXT, " +
                PHONE_COL + " TEXT" +
                ")";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addNewUser(String username, String password, String email, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME_COL, username);
        values.put(PASSWORD_COL, password);
        values.put(EMAIL_COL, email);
        values.put(PHONE_COL, phone);
        long insert = db.insert(TABLE_NAME, null, values);
        db.close();
        return insert;
    }

    @SuppressLint("Range")
    public User getInfoUser(String username, String password) {
        if (username == null || password == null) {
            return null; // Return null if input parameters are null
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        User user = null;

        try {
            String[] columns = {ID_COL, USERNAME_COL, EMAIL_COL, PHONE_COL};
            String condition = USERNAME_COL + " = ? AND " + PASSWORD_COL + " = ?";
            String[] params = {username, password};

            cursor = db.query(
                    TABLE_NAME,
                    columns,
                    condition,
                    params,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(ID_COL)));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(USERNAME_COL)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(EMAIL_COL)));
                user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(PHONE_COL)));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Print error to help debug
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return user; // Return null if user not found
    }
    public boolean checkUserInDatabase(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;

        try {
            String[] columns = {ID_COL, USERNAME_COL, EMAIL_COL, PHONE_COL};
            String condition = USERNAME_COL + " = ? AND " + PASSWORD_COL + " = ?";
            String[] params = {username, password};

            cursor = db.query(
                    TABLE_NAME,
                    columns,
                    condition,
                    params,
                    null,
                    null,
                    null
            );

            exists = cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return exists;
    }

    @SuppressLint("Range")
    public boolean checkUsernameExists(String username) {
        if (username == null) {
            return false; // Return false if username is null
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;

        try {
            String query = "SELECT 1 FROM " + TABLE_NAME + " WHERE " + USERNAME_COL + " = ?";
            cursor = db.rawQuery(query, new String[]{username});
            if (cursor != null && cursor.getCount() > 0) {
                exists = true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return exists;
    }

    @SuppressLint("Range")
    public boolean checkEmailExists(String email) {
        if (email == null) {
            return false; // Return false if email is null
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;

        try {
            String query = "SELECT 1 FROM " + TABLE_NAME + " WHERE " + EMAIL_COL + " = ?";
            cursor = db.rawQuery(query, new String[]{email});
            if (cursor != null && cursor.getCount() > 0) {
                exists = true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return exists;
    }

    @SuppressLint("Range")
    public boolean checkPhoneExists(String phone) {
        if (phone == null) {
            return false; // Return false if phone is null
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;

        try {
            String query = "SELECT 1 FROM " + TABLE_NAME + " WHERE " + PHONE_COL + " = ?";
            cursor = db.rawQuery(query, new String[]{phone});
            if (cursor != null && cursor.getCount() > 0) {
                exists = true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return exists;
    }

}
