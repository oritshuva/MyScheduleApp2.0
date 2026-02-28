package com.example.myscheduleapp20;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SchedulePagerAdapter extends FragmentStateAdapter {

    public SchedulePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return ScheduleListFragment.newInstance("רגיל");
        } else {
            return ScheduleListFragment.newInstance("אחרי בית ספר");
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}