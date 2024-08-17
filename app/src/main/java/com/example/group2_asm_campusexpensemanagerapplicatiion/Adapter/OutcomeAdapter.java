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
import com.example.group2_asm_campusexpensemanagerapplicatiion.Fragment.EditTransactionDialogFragment;
import com.example.group2_asm_campusexpensemanagerapplicatiion.MenuActivity;
import com.example.group2_asm_campusexpensemanagerapplicatiion.R;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

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

                int idColIndex = cursor.getColumnIndexOrThrow(ExpenseDatabase.ID_COL);
                int amountColIndex = cursor.getColumnIndexOrThrow(ExpenseDatabase.AMOUNT_COL);
                int descriptionColIndex = cursor.getColumnIndexOrThrow(ExpenseDatabase.DESCRIPTION_COL);
                int dateColIndex = cursor.getColumnIndexOrThrow(ExpenseDatabase.DATE_COL);
                int typeColIndex = cursor.getColumnIndexOrThrow(ExpenseDatabase.TYPE_COL);

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(idColIndex);
                    double amount = cursor.getDouble(amountColIndex);
                    if (amount < 0) { // Only consider negative amounts as outcomes
                        String description = cursor.getString(descriptionColIndex);
                        String date = cursor.getString(dateColIndex);
                        String type = cursor.getString(typeColIndex);
                        outcomeList.add(new Outcome(id, description, amount, date, type));
                        totalOutcome += amount; // Update total outcome (convert to positive)
                    }
                }
            } finally {
                cursor.close();
                db.close();
            }
            notifyDataSetChanged();
        } else {
            Log.e("OutcomeAdapter", "Cursor is null");
        }
    }

    public void updateData(List<Outcome> newOutcomeList) {
        this.outcomeList.clear();
        if (newOutcomeList != null) {
            this.outcomeList.addAll(newOutcomeList);
        }
        totalOutcome = 0;
        for (Outcome outcome : outcomeList) {
            totalOutcome += -outcome.getAmount(); // Convert to positive
        }
        notifyDataSetChanged();
    }

    public void refreshData() {
        loadOutcomeData();
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
        holder.dateTextView.setText(outcome.getDate());
        holder.amountTextView.setText(String.format("$%.2f", outcome.getAmount())); // Show as positive
        holder.descriptionTextView.setText(outcome.getDescription());

        holder.editButton.setOnClickListener(v -> {
            EditTransactionDialogFragment dialog = EditTransactionDialogFragment.newInstance(
                    outcome.getId(),
                    outcome.getDescription(),
                    outcome.getAmount(),
                    outcome.getDate(),
                    outcome.getType()
            );
            dialog.setTargetFragment(OutcomeAdapter.this, 0);
            dialog.show(((MenuActivity) context).getSupportFragmentManager(), "EditTransactionDialog");
        });

        holder.deleteButton.setOnClickListener(v -> deleteOutcomeItem(outcome.getId(), position));
    }

    @Override
    public int getItemCount() {
        return outcomeList.size();
    }

    public double getTotalOutcome() {
        return totalOutcome; // Return positive total outcome
    }

    private void deleteOutcomeItem(int id, int position) {
        ExpenseDatabase db = new ExpenseDatabase(context);
        int rowsDeleted = db.deleteExpense(id);
        if (rowsDeleted > 0) {
            outcomeList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, outcomeList.size());

            // Recalculate totalOutcome
            totalOutcome = 0;
            for (Outcome outcome : outcomeList) {
                totalOutcome += -outcome.getAmount(); // Convert to positive
            }
        } else {
            Log.e("OutcomeAdapter", "Failed to delete outcome item with ID: " + id);
        }
        db.close();
    }

    static class OutcomeViewHolder extends RecyclerView.ViewHolder {
        TextView descriptionTextView;
        TextView amountTextView;
        TextView dateTextView;
        ImageButton editButton;
        ImageButton deleteButton;

        OutcomeViewHolder(@NonNull View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.outcome_description);
            amountTextView = itemView.findViewById(R.id.outcome_amount);
            dateTextView = itemView.findViewById(R.id.outcome_date);
            editButton = itemView.findViewById(R.id.outcome_edit_button);
            deleteButton = itemView.findViewById(R.id.outcome_delete_button);
        }
    }

    public static class Outcome {
        private int id;
        private String description;
        private double amount;
        private String date;
        private String type;

        public Outcome(int id, String description, double amount, String date, String type) {
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

