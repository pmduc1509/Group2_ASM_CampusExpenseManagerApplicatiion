package com.example.group2_asm_campusexpensemanagerapplicatiion.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group2_asm_campusexpensemanagerapplicatiion.Adapter.IncomeAdapter;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Adapter.OutcomeAdapter;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Database.ExpenseDatabase;
import com.example.group2_asm_campusexpensemanagerapplicatiion.R;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class HomeFragment extends Fragment {

    private TextView totalIncomeTextView;
    private TextView totalOutcomeTextView;
    private TextView remainingMoneyTextView;
    private RecyclerView incomeRecyclerView;
    private RecyclerView outcomeRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private IncomeAdapter incomeAdapter;
    private OutcomeAdapter outcomeAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        totalIncomeTextView = view.findViewById(R.id.total_income_text_view);
        totalOutcomeTextView = view.findViewById(R.id.total_outcome_text_view);
        remainingMoneyTextView = view.findViewById(R.id.remaining_money_text_view);
        incomeRecyclerView = view.findViewById(R.id.income_recycler_view);
        outcomeRecyclerView = view.findViewById(R.id.outcome_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        // Setup RecyclerViews
        incomeAdapter = new IncomeAdapter(getContext());
        incomeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        incomeRecyclerView.setAdapter(incomeAdapter);

        outcomeAdapter = new OutcomeAdapter(getContext());
        outcomeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        outcomeRecyclerView.setAdapter(outcomeAdapter);

        // Load total income, outcome, and remaining money
        loadTotalAmounts();

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadTotalAmounts();
            swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation
        });

        return view;
    }

    private void loadTotalAmounts() {
        ExpenseDatabase db = new ExpenseDatabase(getContext());
        double totalIncome = db.getTotalIncome(); // Get total income
        double totalOutcome = db.getTotalOutcome(); // Get total outcome
        double remainingMoney = totalIncome - totalOutcome; // Calculate remaining money

        totalIncomeTextView.setText("Total Income: $" + String.format("%.2f", totalIncome));
        totalOutcomeTextView.setText("Total Outcome: $" + String.format("%.2f", totalOutcome));
        remainingMoneyTextView.setText("Remaining Money: $" + String.format("%.2f", remainingMoney)); // Update remaining money

        // Refresh data in RecyclerViews
        incomeAdapter.refreshData();
        outcomeAdapter.refreshData();
    }
}
