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

        switch (position) {

            case 0:
                // מערכת שעות בית ספר
                return ScheduleListFragment.newInstance("SCHOOL");

            case 1:
                // מערכת שעות אחרי בית ספר
                return ScheduleListFragment.newInstance("AFTER_SCHOOL");

            case 2:
                // מסך משימות
                return new TaskListFragment();

            default:
                return ScheduleListFragment.newInstance("SCHOOL");
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}