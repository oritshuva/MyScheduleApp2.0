package com.example.myscheduleapp20;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class TemplateSetupActivity extends AppCompatActivity {

    private LinearLayout containerPeriods;
    private EditText etCount;
    private ScheduleTemplateStore templateStore;

    private final List<EditText> periodInputs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_setup);

        templateStore = new ScheduleTemplateStore(this);

        etCount = findViewById(R.id.etCount);
        containerPeriods = findViewById(R.id.containerPeriods);

        Button btnGenerate = findViewById(R.id.btnGenerate);
        Button btnSave = findViewById(R.id.btnSaveTemplate);

        btnGenerate.setOnClickListener(v -> generateInputs());

        btnSave.setOnClickListener(v -> saveTemplate());
    }

    private void generateInputs() {

        containerPeriods.removeAllViews();
        periodInputs.clear();

        String countStr = etCount.getText().toString();
        if (countStr.isEmpty()) return;

        int count = Integer.parseInt(countStr);

        for (int i = 0; i < count; i++) {

            EditText et = new EditText(this);
            et.setHint("שיעור " + (i + 1) + " למשל 08:00-08:45");
            et.setInputType(InputType.TYPE_CLASS_TEXT);

            containerPeriods.addView(et);
            periodInputs.add(et);
        }
    }

    private void saveTemplate() {

        List<String> periods = new ArrayList<>();

        for (EditText et : periodInputs) {
            periods.add(et.getText().toString());
        }

        templateStore.savePeriods(periods);

        finish();
    }
}