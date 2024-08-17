package com.example.group2_asm_campusexpensemanagerapplicatiion.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.Legend;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Database.ExpenseDatabase;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Adapter.IncomeAdapter;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Adapter.OutcomeAdapter;
import com.example.group2_asm_campusexpensemanagerapplicatiion.R;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private TextView totalIncomeTextView;
    private TextView totalOutcomeTextView;
    private TextView remainingMoneyTextView;
    private RecyclerView incomeRecyclerView;
    private RecyclerView outcomeRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BarChart barChart;

    private IncomeAdapter incomeAdapter;
    private OutcomeAdapter outcomeAdapter;
    private ExpenseDatabase expenseDatabase;

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
        barChart = view.findViewById(R.id.barChart);

        expenseDatabase = new ExpenseDatabase(getContext());

        incomeAdapter = new IncomeAdapter(getContext());
        incomeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        incomeRecyclerView.setAdapter(incomeAdapter);

        outcomeAdapter = new OutcomeAdapter(getContext());
        outcomeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        outcomeRecyclerView.setAdapter(outcomeAdapter);

        loadTotalAmounts();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadTotalAmounts();
            swipeRefreshLayout.setRefreshing(false);
        });

        return view;
    }

    private void loadTotalAmounts() {
        if (expenseDatabase != null) {
            double totalIncome = getTotalAmountFromCursor(expenseDatabase.getTotalIncomeCursor());
            double totalOutcome = getTotalAmountFromCursor(expenseDatabase.getTotalOutcomeCursor());
            double remainingMoney = totalIncome + totalOutcome;

            getActivity().runOnUiThread(() -> {
                if (totalIncomeTextView != null) {
                    totalIncomeTextView.setText("Total Income: $" + String.format("%.2f", totalIncome));
                }
                if (totalOutcomeTextView != null) {
                    totalOutcomeTextView.setText("Total Outcome: $" + String.format("%.2f", totalOutcome));
                }
                if (remainingMoneyTextView != null) {
                    remainingMoneyTextView.setText("Remaining Money: $" + String.format("%.2f", remainingMoney));
                }
            });

            updateData();
            updateChart(totalIncome,totalOutcome);
        } else {
            Log.e("HomeFragment", "ExpenseDatabase is not initialized");
        }
    }

    private void updateData() {
        List<IncomeAdapter.Income> incomeData = expenseDatabase.getAllIncomeTransactions();
        incomeAdapter.updateData(incomeData);

        List<OutcomeAdapter.Outcome> outcomeData = expenseDatabase.getAllOutcomeTransactions();
        outcomeAdapter.updateData(outcomeData);
    }

    private void updateChart(double totalIncome, double totalOutcome) {
        if (barChart != null) {
            // Configure BarChart
            barChart.setHorizontalScrollBarEnabled(true);
            barChart.setPinchZoom(true);
            barChart.setDrawBarShadow(false);
            barChart.setDrawGridBackground(false);

            XAxis xAxis = barChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f); // Avoid multiple values on one line

            YAxis leftAxis = barChart.getAxisLeft();
            leftAxis.setGranularity(1f); // Avoid multiple values on one line
            leftAxis.setAxisMinimum(0f); // Start y-axis from zero

            YAxis rightAxis = barChart.getAxisRight();
            rightAxis.setEnabled(false);

            // Configure Legend
            Legend legend = barChart.getLegend();
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setDrawInside(false);

            // Prepare data for BarChart
            List<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(0, (float) totalIncome, "Income"));
            entries.add(new BarEntry(1, (float) -totalOutcome, "Outcome"));

            BarDataSet dataSet = new BarDataSet(entries, "Income vs Outcome");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

            BarData barData = new BarData(dataSet);
            barChart.setData(barData);
            barChart.getDescription().setEnabled(false);
            barChart.animateY(1000); // Animate Y-axis to show changes

            barChart.invalidate(); // Refresh the chart
        } else {
            Log.e("HomeFragment", "BarChart is not initialized");
        }
    }

    @SuppressLint("Range")
    private double getTotalAmountFromCursor(Cursor cursor) {
        double total = 0.0;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    total = cursor.getDouble(cursor.getColumnIndex("total"));
                }
            } catch (Exception e) {
                Log.e("HomeFragment", "Error reading cursor data", e);
            } finally {
                cursor.close();
            }
        } else {
            Log.e("HomeFragment", "Cursor is null");
        }
        return total;
    }

    public void refreshData() {
        loadTotalAmounts();
    }

}
