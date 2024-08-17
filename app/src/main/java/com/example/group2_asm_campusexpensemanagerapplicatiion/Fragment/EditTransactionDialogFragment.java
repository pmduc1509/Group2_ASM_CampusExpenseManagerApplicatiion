package com.example.group2_asm_campusexpensemanagerapplicatiion.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.group2_asm_campusexpensemanagerapplicatiion.Adapter.IncomeAdapter;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Adapter.OutcomeAdapter;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Database.ExpenseDatabase;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Interface.OnTransactionUpdateListener;
import com.example.group2_asm_campusexpensemanagerapplicatiion.R;

import java.util.Calendar;

public class EditTransactionDialogFragment extends DialogFragment {

    private static final String ARG_ID = "id";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_AMOUNT = "amount";
    private static final String ARG_DATE = "date";
    private static final String ARG_TYPE = "type";

    private int id;
    private EditText descriptionEditText;
    private EditText amountEditText;
    private EditText dateEditText;
    private TextView incomeBtn;
    private TextView expenseBtn;
    private String type;
    private OnTransactionUpdateListener updateListener;

    public static EditTransactionDialogFragment newInstance(int id, String description, double amount, String date, String type) {
        EditTransactionDialogFragment fragment = new EditTransactionDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        args.putString(ARG_DESCRIPTION, description);
        args.putDouble(ARG_AMOUNT, amount);
        args.putString(ARG_DATE, date);
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_edit_transaction, null);
        builder.setView(view)
                .setTitle("Edit Transaction")
                .setPositiveButton("Save", (dialog, which) -> saveTransaction())
                .setNegativeButton("Cancel", (dialog, which) -> dismiss());

        descriptionEditText = view.findViewById(R.id.edit_description);
        amountEditText = view.findViewById(R.id.edit_amount);
        dateEditText = view.findViewById(R.id.edit_date);
        incomeBtn = view.findViewById(R.id.incomeBtn);
        expenseBtn = view.findViewById(R.id.expenseBtn);

        if (getArguments() != null) {
            id = getArguments().getInt(ARG_ID);
            descriptionEditText.setText(getArguments().getString(ARG_DESCRIPTION));
            amountEditText.setText(String.valueOf(getArguments().getDouble(ARG_AMOUNT)));
            dateEditText.setText(getArguments().getString(ARG_DATE));
            type = getArguments().getString(ARG_TYPE);
        }

        dateEditText.setOnClickListener(v -> showDatePickerDialog());

        updateButtonSelection();

        incomeBtn.setOnClickListener(v -> {
            type = "income";
            updateButtonSelection();
        });

        expenseBtn.setOnClickListener(v -> {
            type = "expense";
            updateButtonSelection();
        });

        return builder.create();
    }

    private void updateButtonSelection() {
        if ("income".equalsIgnoreCase(type)) {
            setSelectedButton(incomeBtn, expenseBtn);
        } else if ("expense".equalsIgnoreCase(type)) {
            setSelectedButton(expenseBtn, incomeBtn);
        }
    }

    private void saveTransaction() {
        String description = descriptionEditText.getText().toString();
        String amountStr = amountEditText.getText().toString();
        String date = dateEditText.getText().toString();

        if (TextUtils.isEmpty(description) || TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(date)) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("expense".equalsIgnoreCase(type)) {
            amount = -Math.abs(amount);
        } else {
            amount = Math.abs(amount);
        }

        Log.d("EditTransaction", "Saving transaction with type: " + type);

        ExpenseDatabase db = new ExpenseDatabase(requireContext());
        int rowsUpdated = db.updateExpense(id, description, amount, date, type);

        if (rowsUpdated > 0) {
            Toast.makeText(requireContext(), "Transaction updated", Toast.LENGTH_SHORT).show();
            if (updateListener != null) {
                updateListener.onTransactionUpdated();
            }
        } else {
            Toast.makeText(requireContext(), "Failed to update transaction", Toast.LENGTH_SHORT).show();
        }

        dismiss();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, month1, dayOfMonth) -> {
                    String date = String.format("%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                    dateEditText.setText(date);
                }, year, month, day);

        datePickerDialog.show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            updateListener = (OnTransactionUpdateListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnTransactionUpdateListener");
        }
    }

    @SuppressLint("ResourceAsColor")
    private void setSelectedButton(TextView selectedButton, TextView unselectedButton) {
        selectedButton.setBackgroundResource(R.drawable.income_selector);
        unselectedButton.setBackgroundResource(R.drawable.expense_selector);
    }

    public void setTargetFragment(IncomeAdapter incomeAdapter, int requestCode) {
    }

    public void setTargetFragment(OutcomeAdapter outcomeAdapter, int i) {
    }
}
