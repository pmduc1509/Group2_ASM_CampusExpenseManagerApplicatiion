package com.example.group2_asm_campusexpensemanagerapplicatiion;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.group2_asm_campusexpensemanagerapplicatiion.Fragment.ProfileFragment;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Fragment.ExpenseFragment;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Fragment.HomeFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new ExpenseFragment();
            case 2:
                return new ProfileFragment();
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Number of pages
    }
}
