package com.example.group2_asm_campusexpensemanagerapplicatiion.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.group2_asm_campusexpensemanagerapplicatiion.R;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Database.UserDatabase;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Models.User;
import com.example.group2_asm_campusexpensemanagerapplicatiion.SignInActivity;

public class ProfileFragment extends Fragment {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";

    private TextView usernameTextView;
    private TextView emailTextView;
    private TextView phoneTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        usernameTextView = view.findViewById(R.id.usernameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        Button logoutButton = view.findViewById(R.id.logoutButton);

        // Load user information from SharedPreferences
        loadUserInfo();

        // Set up logout button click listener
        logoutButton.setOnClickListener(v -> {
            // Clear user data from SharedPreferences
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Redirect to SignInActivity
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear all activities
            startActivity(intent);
            getActivity().finish(); // Finish current activity
        });

        return view;
    }

    private void loadUserInfo() {
        // Retrieve user info from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(KEY_USERNAME, "N/A");
        String email = sharedPreferences.getString(KEY_EMAIL, "N/A");
        String phone = sharedPreferences.getString(KEY_PHONE, "N/A");

        Log.d("ProfileFragment", "Username: " + username); // Log username for debugging
        Log.d("ProfileFragment", "Email: " + email); // Log email for debugging
        Log.d("ProfileFragment", "Phone: " + phone); // Log phone for debugging

        // Update TextViews with user data
        usernameTextView.setText(username);
        emailTextView.setText(email);
        phoneTextView.setText(phone);
    }
}
