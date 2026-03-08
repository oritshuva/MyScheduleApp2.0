package com.example.myscheduleapp20;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ScheduleActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private SchedulePagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_schedule);

        initViews();
        setupViewPager();
        setupTabs();
    }

    private void initViews() {

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

    }

    private void setupViewPager() {

        adapter = new SchedulePagerAdapter(this);

        viewPager.setAdapter(adapter);

    }

    private void setupTabs() {

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {

                    switch (position) {

                        case 0:
                            tab.setText("לימודים");
                            break;

                        case 1:
                            tab.setText("אחרי בית ספר");
                            break;
                    }

                }).attach();
    }
}