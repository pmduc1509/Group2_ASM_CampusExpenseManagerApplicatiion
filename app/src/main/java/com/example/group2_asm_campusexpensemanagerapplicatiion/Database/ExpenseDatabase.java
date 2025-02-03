package com.example.group2_asm_campusexpensemanagerapplicatiion.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ExpenseDatabase extends SQLiteOpenHelper {
    public static final String DB_NAME = "expense_database";
    public static final int DB_VERSION = 2; // Version 2 includes 'date' column
    public static final String TABLE_NAME = "expenses";
    public static final String ID_COL = "id";
    public static final String DESCRIPTION_COL = "description";
    public static final String AMOUNT_COL = "amount";
    public static final String DATE_COL = "date";

    public ExpenseDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the table with the 'date' column
        String query = "CREATE TABLE " + TABLE_NAME + "(" +
                ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DESCRIPTION_COL + " TEXT, " +
                AMOUNT_COL + " REAL, " +
                DATE_COL + " TEXT)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add the 'date' column if upgrading from version 1 to version 2
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + DATE_COL + " TEXT");
        }
    }

    public long addExpense(String description, double amount, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DESCRIPTION_COL, description);
        values.put(AMOUNT_COL, amount);
        values.put(DATE_COL, date); // Insert the date
        long insert = db.insert(TABLE_NAME, null, values);
        db.close();
        return insert;
    }

    public int updateExpense(int id, String description, double amount, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DESCRIPTION_COL, description);
        values.put(AMOUNT_COL, amount);
        values.put(DATE_COL, date); // Update the date

        int rowsAffected = db.update(TABLE_NAME, values, ID_COL + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected;
    }

    public int deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_NAME, ID_COL + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rowsDeleted;
    }

    public Cursor getExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, DATE_COL + " DESC"); // Order by date descending
    }

    public double getTotalIncome() {
        double total = 0.0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + AMOUNT_COL + ") as total FROM " + TABLE_NAME + " WHERE " + AMOUNT_COL + " > 0", null);
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(cursor.getColumnIndex("total"));
        }
        cursor.close();
        db.close();
        return total;
    }

    public double getTotalOutcome() {
        double total = 0.0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + AMOUNT_COL + ") as total FROM " + TABLE_NAME + " WHERE " + AMOUNT_COL + " < 0", null);
        if (cursor.moveToFirst()) {
            total = -cursor.getDouble(cursor.getColumnIndex("total")); // Convert negative sum to positive
        }
        cursor.close();
        db.close();
        return total;
    }

    public double getRemainingMoney() {
        double totalIncome = getTotalIncome();
        double totalOutcome = getTotalOutcome(); // This already returns positive value
        return totalIncome - totalOutcome;
    }

    public int clearExpenses() {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_NAME, null, null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + TABLE_NAME + "'"); // Reset the ID counter
        db.close();
        return rowsDeleted;
    }

    public boolean checkExpenseIdExists(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, ID_COL + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }
}
