package com.example.group2_asm_campusexpensemanagerapplicatiion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Interface.OnTransactionUpdateListener;
import com.example.group2_asm_campusexpensemanagerapplicatiion.ViewPagerAdapter;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Fragment.ExpenseFragment;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Fragment.ProfileFragment;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Fragment.HomeFragment;

public class MenuActivity extends AppCompatActivity implements OnTransactionUpdateListener {

    private BottomNavigationView bottomNavigationView;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        viewPager = findViewById(R.id.ViewPager);

        // Receive information from Intent
        Intent intent = getIntent();
        if (intent != null) {
            String username = intent.getStringExtra("username");
            String email = intent.getStringExtra("email");
            String phone = intent.getStringExtra("phone");

            if (username == null || email == null || phone == null) {
                Toast.makeText(this, "Missing user information", Toast.LENGTH_SHORT).show();
            } else {
                // Save user information to SharedPreferences
                saveUserInfoToPreferences(username, email, phone);
            }
        }

        setupViewPager();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_home) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.menu_expense) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.menu_profile) {
                viewPager.setCurrentItem(2);
                return true;
            } else {
                return false;
            }
        });
    }

    private void setupViewPager() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Lifecycle lifecycle = getLifecycle();
        viewPagerAdapter = new ViewPagerAdapter(fragmentManager, lifecycle);
        viewPager.setAdapter(viewPagerAdapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        bottomNavigationView.getMenu().findItem(R.id.menu_home).setChecked(true);
                        break;
                    case 1:
                        bottomNavigationView.getMenu().findItem(R.id.menu_expense).setChecked(true);
                        break;
                    case 2:
                        bottomNavigationView.getMenu().findItem(R.id.menu_profile).setChecked(true);
                        break;
                }
            }
        });
    }

    private void saveUserInfoToPreferences(String username, String email, String phone) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putString("phone", phone);
        editor.apply(); // Save the data
    }

    @Override
    public void onTransactionUpdated() {
        // Notify the current fragment in ViewPager2 to refresh data
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
        if (currentFragment instanceof HomeFragment) {
            ((HomeFragment) currentFragment).refreshData();
        } else {
            // Handle other fragment types or errors if needed
            Toast.makeText(this, "Fragment not found or incorrect type", Toast.LENGTH_SHORT).show();
        }
    }
}
