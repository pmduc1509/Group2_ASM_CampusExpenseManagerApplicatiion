package com.example.group2_asm_campusexpensemanagerapplicatiion.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.group2_asm_campusexpensemanagerapplicatiion.Adapter.IncomeAdapter;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Adapter.OutcomeAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseDatabase extends SQLiteOpenHelper {
    public static final String DB_NAME = "expense_database";
    public static final int DB_VERSION = 4; // Updated database version
    public static final String TABLE_NAME = "expenses";
    public static final String ID_COL = "id";
    public static final String DESCRIPTION_COL = "description";
    public static final String AMOUNT_COL = "amount";
    public static final String DATE_COL = "date";
    public static final String TYPE_COL = "type"; // Added type column

    public ExpenseDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table with 'type' column
        String query = "CREATE TABLE " + TABLE_NAME + "(" +
                ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DESCRIPTION_COL + " TEXT, " +
                AMOUNT_COL + " REAL, " +
                DATE_COL + " TEXT, " +
                TYPE_COL + " TEXT DEFAULT 'expense')"; // Added type column with default value
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            // Add 'type' column if it doesn't exist
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + TYPE_COL + " TEXT DEFAULT 'expense'");
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addExpense(String description, double amount, String date, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DESCRIPTION_COL, description);
        values.put(AMOUNT_COL, amount);
        values.put(DATE_COL, date);
        values.put(TYPE_COL, type); // Store transaction type (income or expense)
        long insert = db.insert(TABLE_NAME, null, values);
        db.close();
        return insert;
    }

    public int updateExpense(int id, String description, double amount, String date, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DESCRIPTION_COL, description);
        values.put(AMOUNT_COL, amount);
        values.put(DATE_COL, date);
        values.put(TYPE_COL, type); // Ensure this line is included

        int rowsUpdated = db.update(TABLE_NAME, values, ID_COL + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsUpdated;
    }

    public int deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_NAME, ID_COL + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rowsDeleted;
    }

    public Cursor getExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, DATE_COL + " DESC");
    }

    public double getTotalIncome() {
        double total = 0.0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + AMOUNT_COL + ") as total FROM " + TABLE_NAME + " WHERE " + TYPE_COL + " = 'income'", null);
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(cursor.getColumnIndex("total"));
        } else {
            Log.w("ExpenseDatabase", "No income data found.");
        }
        cursor.close();
        db.close();
        return total;
    }

    public double getTotalOutcome() {
        double total = 0.0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + AMOUNT_COL + ") as total FROM " + TABLE_NAME + " WHERE " + TYPE_COL + " = 'expense'", null);
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(cursor.getColumnIndex("total"));
        } else {
            Log.w("ExpenseDatabase", "No outcome data found.");
        }
        cursor.close();
        db.close();
        return total;
    }

    public double getRemainingMoney() {
        double totalIncome = getTotalIncome();
        double totalOutcome = getTotalOutcome();
        return totalIncome - totalOutcome;
    }

    public int clearExpenses() {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_NAME, null, null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + TABLE_NAME + "'");
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

    public Cursor getExpenseById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, ID_COL + " = ?", new String[]{String.valueOf(id)}, null, null, null);
    }

    public Cursor getAllExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + DATE_COL + " DESC", null);
    }

    public Cursor getTotalIncomeCursor() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT SUM(" + AMOUNT_COL + ") as total FROM " + TABLE_NAME + " WHERE " + TYPE_COL + " = 'income'", null);
    }

    public Cursor getTotalOutcomeCursor() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT SUM(" + AMOUNT_COL + ") as total FROM " + TABLE_NAME + " WHERE " + TYPE_COL + " = 'expense'", null);
    }

    // New methods to get all transactions of each type

    public List<IncomeAdapter.Income> getAllIncomeTransactions() {
        List<IncomeAdapter.Income> incomeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, TYPE_COL + " = ?", new String[]{"income"}, null, null, DATE_COL + " DESC");
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(ID_COL));
                String description = cursor.getString(cursor.getColumnIndex(DESCRIPTION_COL));
                double amount = cursor.getDouble(cursor.getColumnIndex(AMOUNT_COL));
                String date = cursor.getString(cursor.getColumnIndex(DATE_COL));
                IncomeAdapter.Income income = new IncomeAdapter.Income(id, description, amount, date, "income");
                incomeList.add(income);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return incomeList;
    }

    public List<OutcomeAdapter.Outcome> getAllOutcomeTransactions() {
        List<OutcomeAdapter.Outcome> outcomeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, TYPE_COL + " = ?", new String[]{"expense"}, null, null, DATE_COL + " DESC");
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(ID_COL));
                String description = cursor.getString(cursor.getColumnIndex(DESCRIPTION_COL));
                double amount = cursor.getDouble(cursor.getColumnIndex(AMOUNT_COL));
                String date = cursor.getString(cursor.getColumnIndex(DATE_COL));
                OutcomeAdapter.Outcome outcome = new OutcomeAdapter.Outcome(id, description, amount, date, "expense");
                outcomeList.add(outcome);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return outcomeList;
    }
}
