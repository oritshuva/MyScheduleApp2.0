package com.example.myscheduleapp20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ScheduleActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private ImageButton btnLogout;

    private TextView tvUserGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {

            Intent intent = new Intent(this, LoginActivity.class);

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            return;
        }

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        btnLogout = findViewById(R.id.btnLogout);

        tvUserGreeting = findViewById(R.id.tvUserGreeting);

        if (user.getEmail() != null) {
            tvUserGreeting.setText("שלום " + user.getEmail());
        }

        SchedulePagerAdapter adapter = new SchedulePagerAdapter(this);

        viewPager.setAdapter(adapter);

        new TabLayoutMediator(
                tabLayout,
                viewPager,
                (tab, position) -> {

                    if (position == 0) {
                        tab.setText("מערכת שעות");
                    } else {
                        tab.setText("משימות");
                    }

                }
        ).attach();

        btnLogout.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(
                    ScheduleActivity.this,
                    LoginActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);

        });
    }
}