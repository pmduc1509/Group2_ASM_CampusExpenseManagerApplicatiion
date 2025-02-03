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

public class OutcomeAdapter extends RecyclerView.Adapter<OutcomeAdapter.OutcomeViewHolder> {

    private Context context;
    private List<Outcome> outcomeList;
    private double totalOutcome;

    public OutcomeAdapter(Context context) {
        this.context = context;
        this.outcomeList = new ArrayList<>();
        loadOutcomeData();
    }

    private void loadOutcomeData() {
        ExpenseDatabase db = new ExpenseDatabase(context);
        Cursor cursor = db.getExpenses();

        if (cursor != null) {
            try {
                totalOutcome = 0; // Reset total outcome
                outcomeList.clear(); // Clear existing data

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(ExpenseDatabase.ID_COL)); // Get ID
                    double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(ExpenseDatabase.AMOUNT_COL));
                    if (amount < 0) {
                        String description = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseDatabase.DESCRIPTION_COL));
                        String date = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseDatabase.DATE_COL)); // Get the date
                        outcomeList.add(new Outcome(id, description, amount, date));
                        totalOutcome += amount; // Calculate total outcome
                    }
                }
            } finally {
                cursor.close();
                db.close();
            }
        }
        notifyDataSetChanged(); // Notify RecyclerView that data has changed
    }

    // Method to refresh data
    public void refreshData() {
        loadOutcomeData(); // Reload the data
    }

    @NonNull
    @Override
    public OutcomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_outcome, parent, false);
        return new OutcomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OutcomeViewHolder holder, int position) {
        Outcome outcome = outcomeList.get(position);
        holder.descriptionTextView.setText(outcome.getDescription());
        holder.amountTextView.setText(String.format("$%.2f", -outcome.getAmount())); // Display as positive amount
        holder.dateTextView.setText(outcome.getDate()); // Set the date

        holder.deleteButton.setOnClickListener(v -> {
            deleteOutcomeItem(outcome.getId(), position);
        });
    }

    @Override
    public int getItemCount() {
        return outcomeList.size();
    }

    public double getTotalOutcome() {
        return -totalOutcome; // Return positive total outcome
    }

    private void deleteOutcomeItem(int id, int position) {
        ExpenseDatabase db = new ExpenseDatabase(context);
        int rowsDeleted = db.deleteExpense(id);
        if (rowsDeleted > 0) {
            Log.d("OutcomeAdapter", "Deleted outcome item with ID: " + id);
            outcomeList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, outcomeList.size());

            // Recalculate totalOutcome
            totalOutcome = 0;
            for (Outcome outcome : outcomeList) {
                totalOutcome += outcome.getAmount();
            }

            Log.d("OutcomeAdapter", "Total outcome updated: " + totalOutcome);
        } else {
            Log.e("OutcomeAdapter", "Failed to delete outcome item with ID: " + id);
        }
        db.close(); // Ensure the database is closed
    }

    static class OutcomeViewHolder extends RecyclerView.ViewHolder {
        TextView descriptionTextView;
        TextView amountTextView;
        TextView dateTextView; // Add a TextView for date
        ImageButton deleteButton; // Add a delete button

        OutcomeViewHolder(@NonNull View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.outcome_description);
            amountTextView = itemView.findViewById(R.id.outcome_amount);
            dateTextView = itemView.findViewById(R.id.outcome_date); // Initialize the date TextView
            deleteButton = itemView.findViewById(R.id.outcome_delete_button); // Initialize the delete button
        }
    }

    public static class Outcome {
        private int id; // Add an ID field
        private String description;
        private double amount;
        private String date; // Add date field

        public Outcome(int id, String description, double amount, String date) {
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
