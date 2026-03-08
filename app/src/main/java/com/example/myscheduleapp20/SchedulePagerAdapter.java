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
                return ScheduleListFragment.newInstance("school", "ראשון");

            case 1:
                return ScheduleListFragment.newInstance("school", "שני");

            case 2:
                return ScheduleListFragment.newInstance("school", "שלישי");

            case 3:
                return ScheduleListFragment.newInstance("school", "רביעי");

            case 4:
                return ScheduleListFragment.newInstance("school", "חמישי");

            case 5:
                return ScheduleListFragment.newInstance("school", "שישי");

            case 6:
                return ScheduleListFragment.newInstance("school", "שבת");

            default:
                return ScheduleListFragment.newInstance("school", "ראשון");
        }
    }

    @Override
    public int getItemCount() {
        return 7;
    }
}