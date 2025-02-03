package com.example.group2_asm_campusexpensemanagerapplicatiion.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group2_asm_campusexpensemanagerapplicatiion.Database.ExpenseDatabase;
import com.example.group2_asm_campusexpensemanagerapplicatiion.R;

import java.util.ArrayList;
import java.util.List;

public class IncomeAdapter extends RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder> {

    private Context context;
    private List<Income> incomeList;

    public IncomeAdapter(Context context) {
        this.context = context;
        this.incomeList = new ArrayList<>();
        loadIncomeData(); // Load initial data
    }

    private void loadIncomeData() {
        ExpenseDatabase db = new ExpenseDatabase(context);
        Cursor cursor = db.getExpenses();

        if (cursor != null) {
            try {
                incomeList.clear(); // Clear current list before loading new data
                int idColIndex = cursor.getColumnIndex(ExpenseDatabase.ID_COL);
                int amountColIndex = cursor.getColumnIndex(ExpenseDatabase.AMOUNT_COL);
                int descriptionColIndex = cursor.getColumnIndex(ExpenseDatabase.DESCRIPTION_COL);
                int dateColIndex = cursor.getColumnIndex(ExpenseDatabase.DATE_COL);

                if (idColIndex != -1 && amountColIndex != -1 && descriptionColIndex != -1 && dateColIndex != -1) {
                    while (cursor.moveToNext()) {
                        int id = cursor.getInt(idColIndex);
                        double amount = cursor.getDouble(amountColIndex);
                        if (amount > 0) {
                            String description = cursor.getString(descriptionColIndex);
                            String date = cursor.getString(dateColIndex); // Get the date
                            incomeList.add(new Income(id, description, amount, date));
                        }
                    }
                } else {
                    // Log error or handle missing columns
                    Log.e("IncomeAdapter", "One or more columns not found");
                }
            } finally {
                cursor.close();
                db.close();
            }
            notifyDataSetChanged(); // Notify RecyclerView that data has changed
        } else {
            // Log error or handle null cursor
            Log.e("IncomeAdapter", "Cursor is null");
        }
    }

    public void refreshData() {
        loadIncomeData(); // Reload the data
    }

    @NonNull
    @Override
    public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_income, parent, false);
        return new IncomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncomeViewHolder holder, int position) {
        Income income = incomeList.get(position);
        holder.dateTextView.setText(income.getDate());
        holder.amountTextView.setText(String.format("$%.2f", income.getAmount()));
        holder.descriptionTextView.setText(income.getDescription());

        holder.deleteButton.setOnClickListener(v -> {
            deleteIncomeItem(income.getId(), position);
        });
    }

    @Override
    public int getItemCount() {
        return incomeList.size();
    }

    private void deleteIncomeItem(int id, int position) {
        ExpenseDatabase db = new ExpenseDatabase(context);
        int rowsDeleted = db.deleteExpense(id);
        if (rowsDeleted > 0) {
            Log.d("IncomeAdapter", "Deleted income item with ID: " + id);
            incomeList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, incomeList.size());
        } else {
            Log.e("IncomeAdapter", "Failed to delete income item with ID: " + id);
        }
    }

    static class IncomeViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView amountTextView;
        TextView descriptionTextView;
        ImageButton deleteButton;

        IncomeViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.income_date);
            amountTextView = itemView.findViewById(R.id.income_amount);
            descriptionTextView = itemView.findViewById(R.id.income_description);
            deleteButton = itemView.findViewById(R.id.income_delete_button);
        }
    }

    public static class Income {
        private int id;
        private String description;
        private double amount;
        private String date;

        public Income(int id, String description, double amount, String date) {
            this.id = id;
            this.description = description;
            this.amount = amount;
            this.date = date;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public double getAmount() {
            return amount;
        }

        public String getDate() {
            return date;
        }
    }
}
