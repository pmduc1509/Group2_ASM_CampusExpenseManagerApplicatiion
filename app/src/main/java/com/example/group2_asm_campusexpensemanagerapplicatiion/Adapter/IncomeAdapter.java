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
import com.example.group2_asm_campusexpensemanagerapplicatiion.Fragment.EditTransactionDialogFragment;
import com.example.group2_asm_campusexpensemanagerapplicatiion.MenuActivity;

import java.util.ArrayList;
import java.util.List;

public class IncomeAdapter extends RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder> {
    private Context context;
    private List<Income> incomeList;
    private double totalIncome;

    public IncomeAdapter(Context context) {
        this.context = context;
        this.incomeList = new ArrayList<>();
        loadIncomeData();
    }

    private void loadIncomeData() {
        ExpenseDatabase db = new ExpenseDatabase(context);
        Cursor cursor = null;
        try {
            cursor = db.getExpenses();
            if (cursor != null) {
                totalIncome = 0; // Reset total income
                incomeList.clear(); // Clear existing data

                int idColIndex = cursor.getColumnIndexOrThrow(ExpenseDatabase.ID_COL);
                int amountColIndex = cursor.getColumnIndexOrThrow(ExpenseDatabase.AMOUNT_COL);
                int descriptionColIndex = cursor.getColumnIndexOrThrow(ExpenseDatabase.DESCRIPTION_COL);
                int dateColIndex = cursor.getColumnIndexOrThrow(ExpenseDatabase.DATE_COL);
                int typeColIndex = cursor.getColumnIndexOrThrow(ExpenseDatabase.TYPE_COL);

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(idColIndex);
                    double amount = cursor.getDouble(amountColIndex);
                    if (amount > 0) { // Only consider positive amounts
                        String description = cursor.getString(descriptionColIndex);
                        String date = cursor.getString(dateColIndex);
                        String type = cursor.getString(typeColIndex);
                        incomeList.add(new Income(id, description, amount, date, type));
                        totalIncome += amount; // Update total income
                    }
                }
                notifyDataSetChanged();
            } else {
                Log.e("IncomeAdapter", "Cursor is null");
            }
        } catch (Exception e) {
            Log.e("IncomeAdapter", "Error loading income data", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public void updateData(List<Income> newIncomeList) {
        this.incomeList.clear();
        if (newIncomeList != null) {
            this.incomeList.addAll(newIncomeList);
        }
        calculateTotalIncome();
        notifyDataSetChanged();
    }

    public void refreshData() {
        loadIncomeData();
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

        holder.editButton.setOnClickListener(v -> {
            EditTransactionDialogFragment dialog = EditTransactionDialogFragment.newInstance(
                    income.getId(),
                    income.getDescription(),
                    income.getAmount(),
                    income.getDate(),
                    income.getType()
            );
            dialog.setTargetFragment(IncomeAdapter.this, 0);
            dialog.show(((MenuActivity) context).getSupportFragmentManager(), "EditTransactionDialog");
        });

        holder.deleteButton.setOnClickListener(v -> deleteIncomeItem(income.getId(), position));
    }

    @Override
    public int getItemCount() {
        return incomeList.size();
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    private void calculateTotalIncome() {
        totalIncome = 0;
        for (Income income : incomeList) {
            totalIncome += income.getAmount();
        }
    }

    private void deleteIncomeItem(int id, int position) {
        ExpenseDatabase db = new ExpenseDatabase(context);
        try {
            int rowsDeleted = db.deleteExpense(id);
            if (rowsDeleted > 0) {
                incomeList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, incomeList.size());

                calculateTotalIncome();
            } else {
                Log.e("IncomeAdapter", "Failed to delete income item with ID: " + id);
            }
        } finally {
            db.close();
        }
    }

    static class IncomeViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView amountTextView;
        TextView descriptionTextView;
        ImageButton editButton;
        ImageButton deleteButton;

        IncomeViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.income_date);
            amountTextView = itemView.findViewById(R.id.income_amount);
            descriptionTextView = itemView.findViewById(R.id.income_description);
            editButton = itemView.findViewById(R.id.income_edit_button);
            deleteButton = itemView.findViewById(R.id.income_delete_button);
        }
    }

    public static class Income {
        private int id;
        private String description;
        private double amount;
        private String date;
        private String type;

        public Income(int id, String description, double amount, String date, String type) {
            this.id = id;
            this.description = description;
            this.amount = amount;
            this.date = date;
            this.type = type;
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

        public String getType() {
            return type;
        }
    }

}
