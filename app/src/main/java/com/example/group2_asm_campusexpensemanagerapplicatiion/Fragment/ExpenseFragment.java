package com.example.group2_asm_campusexpensemanagerapplicatiion.Fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group2_asm_campusexpensemanagerapplicatiion.Adapter.OutcomeAdapter;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Adapter.IncomeAdapter;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Database.ExpenseDatabase;
import com.example.group2_asm_campusexpensemanagerapplicatiion.R;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExpenseFragment extends Fragment {

    private EditText expenseDescriptionEditText;
    private EditText expenseAmountEditText;
    private EditText expenseIdEditText;
    private EditText expenseDateEditText;
    private Button addExpenseButton;
    private Button subtractExpenseButton;
    private Button updateExpenseButton;
    private Button updateSubtractButton;
    private Button viewHistoryButton;
    private Button clearExpensesButton;
    private TextView expenseTextView;
    private TextView totalMoneyTextView;
    private RecyclerView incomeRecyclerView;
    private RecyclerView outcomeRecyclerView;
    private IncomeAdapter incomeAdapter;
    private OutcomeAdapter outcomeAdapter;
    private ExpenseDatabase expenseDatabase;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_expense, container, false);

        // Initialize UI components
        expenseDescriptionEditText = view.findViewById(R.id.expenseDescriptionEditText);
        expenseAmountEditText = view.findViewById(R.id.expenseAmountEditText);
        expenseIdEditText = view.findViewById(R.id.expenseIdEditText);
        expenseDateEditText = view.findViewById(R.id.expenseDateEditText);
        addExpenseButton = view.findViewById(R.id.addExpenseButton);
        subtractExpenseButton = view.findViewById(R.id.subtractExpenseButton);
        updateExpenseButton = view.findViewById(R.id.updateExpenseButton);
        updateSubtractButton = view.findViewById(R.id.updateSubtractButton);
        viewHistoryButton = view.findViewById(R.id.viewHistoryButton);
        clearExpensesButton = view.findViewById(R.id.clearExpensesButton);
        expenseTextView = view.findViewById(R.id.expenseTextView);
        totalMoneyTextView = view.findViewById(R.id.totalMoneyTextView);

        // Initialize RecyclerView and Adapters
        incomeRecyclerView = view.findViewById(R.id.recycler_view_income);
        outcomeRecyclerView = view.findViewById(R.id.recycler_view_outcome);

        incomeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        outcomeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        incomeAdapter = new IncomeAdapter(getContext());
        outcomeAdapter = new OutcomeAdapter(getContext());

        incomeRecyclerView.setAdapter(incomeAdapter);
        outcomeRecyclerView.setAdapter(outcomeAdapter);

        // Initialize Database
        expenseDatabase = new ExpenseDatabase(getContext());

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swiperefreshlayout2);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
                swipeRefreshLayout.setRefreshing(false); // Stop the refreshing animation
            }
        });

        // Load and display total money
        loadTotalMoney();

        // Set button listeners
        addExpenseButton.setOnClickListener(v -> {
            addExpense(true);
            refreshData();
        });
        subtractExpenseButton.setOnClickListener(v -> {
            addExpense(false);
            refreshData();
        });
        updateExpenseButton.setOnClickListener(v -> {
            updateExpense();
            refreshData();
        });
        updateSubtractButton.setOnClickListener(v -> {
            updateExpenseAsSubtract();
            refreshData();
        });
        viewHistoryButton.setOnClickListener(v -> viewHistory());
        clearExpensesButton.setOnClickListener(v -> {
            clearExpenses();
            refreshData();
        });

        return view;
    }

    private void loadTotalMoney() {
        double totalMoney = expenseDatabase.getRemainingMoney();
        totalMoneyTextView.setText("Total Money: $" + String.format("%.2f", totalMoney));
    }

    private void refreshData() {
        incomeAdapter.refreshData();
        outcomeAdapter.refreshData();
    }

    private void addExpense(boolean isAddition) {
        String description = expenseDescriptionEditText.getText().toString();
        String amountStr = expenseAmountEditText.getText().toString();
        String date = expenseDateEditText.getText().toString();

        if (description.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(getContext(), "Please enter description, amount, and date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidDate(date)) {
            Toast.makeText(getContext(), "Invalid date format. Use YYYY-MM-DD", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (!isAddition) {
                amount = -amount;
            }

            long result = expenseDatabase.addExpense(description, amount, date);

            if (result != -1) {
                loadTotalMoney();
                expenseTextView.setText("Expense added: " + description + " - $" + String.format("%.2f", amount) + " on " + date);
                clearInputFields();
                Toast.makeText(getContext(), "Expense added successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to add expense", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid amount format", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateExpense() {
        String idStr = expenseIdEditText.getText().toString();
        String description = expenseDescriptionEditText.getText().toString();
        String amountStr = expenseAmountEditText.getText().toString();
        String date = expenseDateEditText.getText().toString();

        if (description.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(getContext(), "Please enter description, amount, and date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idStr.isEmpty()) {
            Toast.makeText(getContext(), "ID cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidDate(date)) {
            Toast.makeText(getContext(), "Invalid date format. Use YYYY-MM-DD", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            if (!expenseDatabase.checkExpenseIdExists(id)) {
                Toast.makeText(getContext(), "Expense ID does not exist", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);

            int result = expenseDatabase.updateExpense(id, description, amount, date);

            if (result > 0) {
                loadTotalMoney();
                expenseTextView.setText("Expense updated: ID " + id + " - " + description + " - $" + String.format("%.2f", amount) + " on " + date);
                clearInputFields();
                Toast.makeText(getContext(), "Expense updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to update expense", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid ID or amount format", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateExpenseAsSubtract() {
        String idStr = expenseIdEditText.getText().toString();
        String description = expenseDescriptionEditText.getText().toString();
        String amountStr = expenseAmountEditText.getText().toString();
        String date = expenseDateEditText.getText().toString();

        if (amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(getContext(), "Please enter amount and date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idStr.isEmpty()) {
            Toast.makeText(getContext(), "ID cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidDate(date)) {
            Toast.makeText(getContext(), "Invalid date format. Use YYYY-MM-DD", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            if (!expenseDatabase.checkExpenseIdExists(id)) {
                Toast.makeText(getContext(), "Expense ID does not exist", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            amount = -amount;

            int result = expenseDatabase.updateExpense(id, description, amount, date);

            if (result > 0) {
                loadTotalMoney();
                expenseTextView.setText("Expense updated as subtract: ID " + id + " - " + description + " - $" + String.format("%.2f", amount) + " on " + date);
                clearInputFields();
                Toast.makeText(getContext(), "Expense updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to update expense", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid ID or amount format", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setLenient(false);
        try {
            Date date = sdf.parse(dateStr);
            return date != null;
        } catch (ParseException e) {
            return false;
        }
    }

    private void viewHistory() {
        // Implement logic to view expense history
    }

    private void clearExpenses() {
        // Implement logic to clear all expenses
    }

    private void clearInputFields() {
        expenseDescriptionEditText.setText("");
        expenseAmountEditText.setText("");
        expenseIdEditText.setText("");
        expenseDateEditText.setText("");
    }
}
