package com.example.myscheduleapp20;                       // אותה חבילה כמו MainActivity

import androidx.appcompat.app.AppCompatActivity;           // בסיס לכל Activity עם עיצוב מודרני
import android.os.Bundle;                                  // מעביר מצב ל-onCreate
import android.text.Editable;                              // מייצג טקסט שניתן לעריכה (בשביל TextWatcher)
import android.text.TextWatcher;                           // מאזין לשינויים בטקסט בשדות
import android.widget.Button;                              // כפתורים
import android.widget.EditText;                            // שדות טקסט
import android.widget.Toast;                               // הודעות קצרות קופצות על המסך

public class LoginActivity extends AppCompatActivity {     // הגדרת מסך ההתחברות

    private EditText etEmail;                              // שדה האימייל
    private EditText etPassword;                           // שדה הסיסמה
    private Button btnLoginConfirm;                        // כפתור "התחבר"
    private Button btnBack;                                // כפתור "חזרה"

    @Override
    protected void onCreate(Bundle savedInstanceState) {   // נקודת כניסה למסך
        super.onCreate(savedInstanceState);                // מפעיל את onCreate של האב
        setContentView(R.layout.activity_login);           // מחבר את המסך ל-activity_login.xml

        // קישור רכיבי ה-XML למשתנים בקוד:
        etEmail = findViewById(R.id.etEmail);              // מוצא את EditText של האימייל
        etPassword = findViewById(R.id.etPassword);        // מוצא את EditText של הסיסמה
        btnLoginConfirm = findViewById(R.id.btnLoginConfirm); // מוצא את כפתור "התחבר"
        btnBack = findViewById(R.id.btnBack);              // מוצא את כפתור "חזרה"

        // מאזין לשינויים בשדה האימייל:
        etEmail.addTextChangedListener(new TextWatcher() { // מוסיף TextWatcher – נקרא בכל שינוי טקסט
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // לא צריך לעשות כלום לפני שינוי הטקסט
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInputs();                          // בכל שינוי טקסט – נבדוק אם הקלט תקין
            }

            @Override
            public void afterTextChanged(Editable s) {
                // גם אחרי שינוי אין צורך לעשות כלום – כבר בדקנו ב-onTextChanged
            }
        });

        // מאזין לשינויים בשדה הסיסמה:
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // לא צריך
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInputs();                          // כל שינוי בסיסמה – בדיקה מחדש
            }

            @Override
            public void afterTextChanged(Editable s) {
                // אין צורך
            }
        });

        // מה קורה כשמשתמש לוחץ על "התחבר":
        btnLoginConfirm.setOnClickListener(v -> {          // מאזין ללחיצה על כפתור ההתחברות
            String email = etEmail.getText().toString().trim();   // לוקחים את האימייל שהוזן
            String password = etPassword.getText().toString();    // לוקחים את הסיסמה שהוזנה

            // כאן בעתיד נוסיף התחברות אמיתית ל-Firebase / שרת.
            // כרגע רק מציגים הודעה להוכחת עבודה:
            Toast.makeText(
                    LoginActivity.this,                   // הקונטקסט – המסך הזה
                    "התחברות עם " + email,               // הטקסט שיופיע בהודעה
                    Toast.LENGTH_SHORT                    // אורך התצוגה
            ).show();
        });

        // מה קורה כשמשתמש לוחץ על "חזרה":
        btnBack.setOnClickListener(v -> {                 // מאזין ללחיצה על "חזרה"
            finish();                                     // סוגר את LoginActivity וחוזר למסך הקודם
        });

        validateInputs();                                 // קריאה ראשונית – לדאוג שהכפתור כבוי בתחילת המסך
    }

    /**
     * פונקציה שבודקת אם האימייל והסיסמה תקינים,
     * ומפעילה / מכבה את כפתור "התחבר" בהתאם.
     */
    private void validateInputs() {
        String email = etEmail.getText().toString().trim();   // טקסט האימייל בלי רווחים בקצוות
        String password = etPassword.getText().toString();    // טקסט הסיסמה

        boolean isEmailValid = isValidEmail(email);           // בדיקת תקינות אימייל
        boolean isPasswordValid = password.length() >= 6;     // סיסמה לפחות 6 תווים

        boolean enableButton = isEmailValid && isPasswordValid; // הכפתור יופעל רק אם שני התנאים מתקיימים

        btnLoginConfirm.setEnabled(enableButton);             // הפעלה / כיבוי הכפתור בפועל
    }

    /**
     * בדיקה בסיסית מאוד לפורמט אימייל:
     * מספיק שיש גם '@' וגם '.' בתוך המחרוזת.
     */
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");    // מחזיר true אם האימייל "סביר"
    }
}
