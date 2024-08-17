package com.example.group2_asm_campusexpensemanagerapplicatiion.Fragment;

import android.app.DatePickerDialog;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.group2_asm_campusexpensemanagerapplicatiion.Adapter.OutcomeAdapter;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Adapter.IncomeAdapter;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Database.ExpenseDatabase;
import com.example.group2_asm_campusexpensemanagerapplicatiion.R;

import java.util.Calendar;

import kotlin.reflect.KType;

public class ExpenseFragment extends Fragment {

    private EditText expenseDescriptionEditText;
    private EditText expenseAmountEditText;
    private EditText expenseDateEditText;
    private Button addExpenseButton;
    private Button subtractExpenseButton;
    private Button viewHistoryButton;
    private Button clearExpensesButton;
    private TextView expenseTextView;
    private TextView totalMoneyTextView;
    private RecyclerView incomeRecyclerView;
    private RecyclerView outcomeRecyclerView;
    private IncomeAdapter incomeAdapter;
    private OutcomeAdapter outcomeAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ExpenseDatabase expenseDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);

        // Initialize UI components
        expenseDescriptionEditText = view.findViewById(R.id.expenseDescriptionEditText);
        expenseAmountEditText = view.findViewById(R.id.expenseAmountEditText);
        expenseDateEditText = view.findViewById(R.id.expenseDateEditText);
        addExpenseButton = view.findViewById(R.id.addExpenseButton);
        subtractExpenseButton = view.findViewById(R.id.subtractExpenseButton);
        viewHistoryButton = view.findViewById(R.id.viewHistoryButton);
        clearExpensesButton = view.findViewById(R.id.clearExpensesButton);
        expenseTextView = view.findViewById(R.id.expenseTextView);
        totalMoneyTextView = view.findViewById(R.id.totalMoneyTextView);

        // Initialize RecyclerView and Adapter for Income
        incomeRecyclerView = view.findViewById(R.id.recycler_view_income);
        incomeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        incomeAdapter = new IncomeAdapter(getContext());
        incomeRecyclerView.setAdapter(incomeAdapter);

        // Initialize RecyclerView and Adapter for Outcome
        outcomeRecyclerView = view.findViewById(R.id.recycler_view_outcome);
        outcomeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        outcomeAdapter = new OutcomeAdapter(getContext());
        outcomeRecyclerView.setAdapter(outcomeAdapter);

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swiperefreshlayout2);
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        // Initialize Database
        expenseDatabase = new ExpenseDatabase(getContext());

        // Load and display total money
        loadTotalMoney();

        // Set button listeners
        addExpenseButton.setOnClickListener(v -> {
            addExpense(true);
            refreshAdapters();
        });
        subtractExpenseButton.setOnClickListener(v -> {
            addExpense(false);
            refreshAdapters();
        });
        viewHistoryButton.setOnClickListener(v -> viewHistory());
        clearExpensesButton.setOnClickListener(v -> {
            clearExpenses();
            refreshAdapters();
        });

        // Set DatePickerDialog on EditText
        expenseDateEditText.setOnClickListener(v -> showDatePickerDialog());

        return view;
    }

    private void refreshData() {
        // Load and update data
        loadTotalMoney();
        refreshAdapters();

        // Stop the refresh animation
        swipeRefreshLayout.setRefreshing(false);
    }

    private void loadTotalMoney() {
        double totalMoney = expenseDatabase.getRemainingMoney();
        totalMoneyTextView.setText("Total Money: $" + String.format("%.2f", totalMoney));
    }

    private void addExpense(boolean isAddition) {
        String description = expenseDescriptionEditText.getText().toString();
        String amountStr = expenseAmountEditText.getText().toString();
        String date = expenseDateEditText.getText().toString();
        String type = isAddition ? "income" : "expense";

        if (description.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(getContext(), "Please enter description, amount, and date", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (!isAddition) {
                amount = -amount;
            }

            long result = expenseDatabase.addExpense(description, amount, date, type);

            if (result != -1) {
                loadTotalMoney();
                expenseTextView.setText("Expense added: " + description + " - $" + String.format("%.2f", amount) + " on " + date);

                expenseDescriptionEditText.setText("");
                expenseAmountEditText.setText("");
                expenseDateEditText.setText("");

                Toast.makeText(getContext(), "Expense added successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to add expense", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid amount format", Toast.LENGTH_SHORT).show();
        }
    }



    private void viewHistory() {
        Cursor cursor = expenseDatabase.getAllExpenses();
        StringBuilder history = new StringBuilder();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String description = cursor.getString(1);
            double amount = cursor.getDouble(2);
            String date = cursor.getString(3);
            String type = cursor.getString(4);
            history.append("ID: ").append(id).append("\n");
            history.append("Description: ").append(description).append("\n");
            history.append("Amount: $").append(String.format("%.2f", amount)).append("\n");
            history.append("Date: ").append(date).append("\n");
            history.append("Type: ").append(type).append("\n");
            history.append("-------------\n");
        }

        cursor.close();

        if (history.length() > 0) {
            expenseTextView.setText(history.toString());
        } else {
            expenseTextView.setText("No expense history found.");
        }
    }

    private void clearExpenses() {
        expenseDatabase.clearExpenses();
        loadTotalMoney();
        expenseTextView.setText("All expenses cleared.");
    }



    private void refreshAdapters() {
        incomeAdapter.refreshData();
        outcomeAdapter.refreshData();
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth) -> {
            String selectedDate = year1 + "-" + (month1 + 1) + "-" + dayOfMonth;
            expenseDateEditText.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }
}
